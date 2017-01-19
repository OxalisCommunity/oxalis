/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language
 *  governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.as2.outbound;

import brave.Span;
import brave.Tracer;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.httpclient.BraveHttpRequestInterceptor;
import com.github.kristofa.brave.httpclient.BraveHttpResponseInterceptor;
import com.google.inject.Inject;
import eu.peppol.as2.model.As2DispositionNotificationOptions;
import eu.peppol.as2.model.As2Header;
import eu.peppol.as2.model.Mic;
import eu.peppol.as2.util.*;
import eu.peppol.identifier.MessageId;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.security.CertificateUtils;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampService;
import no.difi.oxalis.commons.tracing.Traceable;
import no.difi.vefa.peppol.common.model.Endpoint;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.SSLHandshakeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Thread safe implementation of a {@link MessageSender}, which sends messages using the AS2 protocol.
 * Stores the outbound MIC for verification against the mic received from the MDN later.
 *
 * @author steinar
 * @author thore
 * @author erlend
 */
class As2MessageSender extends Traceable implements MessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(As2MessageSender.class);

    private final PoolingHttpClientConnectionManager httpClientConnectionManager;

    // "SSLv3" is disabled by default : http://www.apache.org/dist/httpcomponents/httpclient/RELEASE_NOTES-4.3.x.txt
    private final SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());

    private final SMimeMessageFactory sMimeMessageFactory;

    private final TimestampService timestampService;

    private final Brave brave;

    private final String fromIdentifier;

    @Inject
    public As2MessageSender(X509Certificate certificate, SMimeMessageFactory sMimeMessageFactory, TimestampService timestampService,
                            Tracer tracer, Brave brave) {
        super(tracer);
        this.sMimeMessageFactory = sMimeMessageFactory;
        this.timestampService = timestampService;
        this.brave = brave;

        // Establishes our AS2 System Identifier based upon the contents of the CN= field of the certificate
        this.fromIdentifier = CertificateUtils.extractCommonName(certificate);

        // Setting up Http Connection pool.
        this.httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        this.httpClientConnectionManager.setDefaultMaxPerRoute(10);
    }

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest) throws OxalisTransmissionException {
        try (Span span = tracer.newTrace().name(getClass().getSimpleName()).start()) {
            try {
                return send(transmissionRequest, span);
            } catch (OxalisTransmissionException e) {
                span.tag("exception", e.getMessage());
                throw e;
            }
        }
    }

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest, Span root)
            throws OxalisTransmissionException {

        try (Span span = tracer.newChild(root.context()).name("Send AS2 message").start()) {
            byte[] mdn = perform(
                    transmissionRequest.getPayload(),
                    transmissionRequest.getMessageId(),
                    transmissionRequest.getEndpoint(),
                    span
            );

            return new As2TransmissionResponse(transmissionRequest, mdn, new Date()); // TODO
        }
    }

    /**
     * This is the work horse method of this class, responsible for the actual http transmission.
     *
     * @throws OxalisTransmissionException
     */
    @SuppressWarnings("unchecked")
    protected byte[] perform(InputStream inputStream,
                                 MessageId messageId,
                                 Endpoint endpoint,
                                 Span root) throws OxalisTransmissionException {

        final String endpointAddress;
        final Mic mic;
        final HttpPost httpPost;

        try (Span span = tracer.newChild(root.context()).name("request").start()) {
            try {
                if (messageId == null) {
                    throw new NullPointerException("MessageId required argument");
                }

                MimeMessage signedMimeMessage;
                try {
                    MimeBodyPart mimeBodyPart = MimeMessageHelper.createMimeBodyPart(inputStream, new MimeType("application/xml"));
                    mic = MimeMessageHelper.calculateMic(mimeBodyPart);
                    LOGGER.debug("Outbound MIC is : " + mic.toString());
                    span.tag("mic", mic.toString());
                    signedMimeMessage = sMimeMessageFactory.createSignedMimeMessage(mimeBodyPart);
                } catch (MimeTypeParseException e) {
                    throw new IllegalStateException("Problems with MIME types: " + e.getMessage(), e);
                }

                endpointAddress = endpoint.getAddress().toString();
                httpPost = new HttpPost(endpointAddress);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    // Get all headers in S/MIME message.
                    List<javax.mail.Header> smimeHeaders = Collections.list(signedMimeMessage.getAllHeaders());

                    List<String> headerNames = smimeHeaders.stream()
                            // Tag for tracing.
                            .peek(h -> span.tag(h.getName(), h.getValue()))
                                    // Add headers to httpPost object.
                            .peek(h -> httpPost.addHeader(h.getName(), h.getValue().replace("\r\n\t", "")))
                                    // Collect header names....
                            .map(javax.mail.Header::getName)
                                    // ... in a list.
                            .collect(Collectors.toList());

                    // Write content to OutputStream without headers.
                    signedMimeMessage.writeTo(byteArrayOutputStream, headerNames.toArray(new String[headerNames.size()]));
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to stream S/MIME message into byte array output stream");
                }

                // Detect certificate "Common Name" (CN) to be used as "AS2-To" value. Used "unknown" if no certificate
                // is found.
                String receiverName = endpoint.getCertificate() != null ?
                        CertificateUtils.extractCommonName(endpoint.getCertificate()) : "unknown";

                // Set all headers specific to AS2 (not MIME).
                httpPost.addHeader(As2Header.AS2_FROM.getHttpHeaderName(), fromIdentifier);
                httpPost.setHeader(As2Header.AS2_TO.getHttpHeaderName(), receiverName);
                httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_TO.getHttpHeaderName(), "not.in.use@difi.no");
                httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(),
                        As2DispositionNotificationOptions.getDefault().toString());
                httpPost.addHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
                httpPost.addHeader(As2Header.SUBJECT.getHttpHeaderName(), "AS2 message from OXALIS");
                httpPost.addHeader(As2Header.DATE.getHttpHeaderName(), As2DateUtil.format(new Date()));

                // Inserts the S/MIME message to be posted.
                // Make sure we pass the same content type as the SignedMimeMessage, it'll end up as content-type HTTP header
                httpPost.setEntity(new ByteArrayEntity(byteArrayOutputStream.toByteArray()));
            } catch (RuntimeException e) {
                span.tag("exception", e.getMessage());
                throw e;
            }
        }

        // EXECUTE !!!!
        CloseableHttpResponse response;
        Timestamp t3;
        try (Span span = tracer.newChild(root.context()).name("execute").start()) {
            try {
                span.tag("endpoint url", endpointAddress);

                CloseableHttpClient httpClient = getHttpClient(span);

                response = httpClient.execute(httpPost);

                t3 = timestampService.generate(mic.toString().getBytes());
            } catch (HttpHostConnectException e) {
                span.tag("exception", e.getMessage());
                throw new OxalisTransmissionException("Oxalis server does not seem to be running.", endpoint.getAddress(), e);
            } catch (SSLHandshakeException e) {
                span.tag("exception", e.getMessage());
                throw new OxalisTransmissionException("Possible invalid SSL Certificate at the other end.", endpoint.getAddress(), e);
            } catch (Exception e) {
                span.tag("exception", e.getMessage());
                throw new OxalisTransmissionException(endpoint.getAddress(), e);
            }
        }

        try (Span span = tracer.newChild(root.context()).name("response").start()) {
            try {
                span.tag("code", String.valueOf(response.getStatusLine().getStatusCode()));

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    LOGGER.error("AS2 HTTP POST expected HTTP OK, but got : {} from {}", response.getStatusLine().getStatusCode(), endpointAddress);
                    throw handleFailedRequest(response);
                }

                // handle normal HTTP OK response
                LOGGER.debug("AS2 transmission {} to {} returned HTTP OK, verify MDN response", messageId, endpointAddress);
                MimeMessage signedMimeMDN = handleTheHttpResponse(mic, response, endpoint);

                return MimeMessageHelper.toBytes(signedMimeMDN);
            } catch (RuntimeException e) {
                span.tag("exception", e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Handles the HTTP 200 POST response (the MDN with status indications)
     *
     * @param outboundMic  the calculated mic of the payload (should be verified against the one returned in MDN)
     * @param closeableHttpResponse the http response to be decoded as MDN
     */
    protected MimeMessage handleTheHttpResponse(Mic outboundMic, CloseableHttpResponse closeableHttpResponse, Endpoint endpoint) {
        try (CloseableHttpResponse postResponse = closeableHttpResponse) {
            // Prepare calculation of message digest.
            MessageDigest digest = MessageDigest.getInstance("sha1", BouncyCastleProvider.PROVIDER_NAME);
            DigestInputStream digestInputStream = new DigestInputStream(postResponse.getEntity().getContent(), digest);

            Header contentTypeHeader = postResponse.getFirstHeader("Content-Type");
            if (contentTypeHeader == null)
                throw new IllegalStateException("No Content-Type header in response, probably a server error");

            MimeMessage mimeMessage;
            try {
                mimeMessage = MimeMessageHelper.parseMultipart(digestInputStream, new MimeType(contentTypeHeader.getValue()));

                dumpMdnToLogger(mimeMessage);

            } catch (MimeTypeParseException e) {
                throw new IllegalStateException("Invalid Content-Type header");
            }

            // verify the signature of the MDN, we warn about dodgy signatures
            try {
                SignedMimeMessage signedMimeMessage = new SignedMimeMessage(mimeMessage);
                X509Certificate cert = signedMimeMessage.getSignersX509Certificate();

                // Verify if the certificate used by the receiving Access Point in
                // the response message does not match its certificate published by the SMP
                if (endpoint.getCertificate() != null && !endpoint.getCertificate().equals(cert)) {
                    throw new CertificateException("Common name in certificate from SMP does not match common name in AP certificate");
                }

                LOGGER.debug("MDN signature was verfied for : " + cert.getSubjectDN().toString());
            } catch (Exception ex) {
                LOGGER.warn("Exception when verifying MDN signature : " + ex.getMessage());
            }

            // Verifies the actual MDN
            MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
            String msg = mdnMimeMessageInspector.getPlainTextPartAsText();

            if (mdnMimeMessageInspector.isOkOrWarning(outboundMic)) {
                // TODO: save the native transport evidence.
                return mimeMessage;
            } else {
                LOGGER.error("AS2 transmission failed with some error message '{}'.", msg);
                throw new IllegalStateException("AS2 transmission failed : " + msg);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to obtain the contents of the response: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new IllegalStateException("Unable to parse received content.", e);
        }
    }

    private void dumpMdnToLogger(MimeMessage mimeMessage) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("======================================================");
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mimeMessage.writeTo(baos);
                LOGGER.debug(baos.toString());
            } catch (MessagingException e) {
                throw new IllegalStateException("Unable to print mime message");
            }
            LOGGER.debug("======================================================");
        }
    }

    protected IllegalStateException handleFailedRequest(CloseableHttpResponse postResponse) {
        HttpEntity entity = postResponse.getEntity();   // Any results?
        try {
            if (entity == null) {
                // No content returned
                throw new IllegalStateException("Request failed with rc=" + postResponse.getStatusLine().getStatusCode() + ", no content returned in HTTP response");
            } else {
                String contents = EntityUtils.toString(entity);
                throw new IllegalStateException("Request failed with rc=" + postResponse.getStatusLine().getStatusCode() + ", contents received (" + contents.trim().length() + " characters):" + contents);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Request failed with rc=" + postResponse.getStatusLine().getStatusCode()
                    + ", ERROR while retrieving the contents of the response:" + e.getMessage(), e);
        }
    }

    protected CloseableHttpClient getHttpClient(Span root) {
        return HttpClients.custom()
                .addInterceptorFirst(BraveHttpRequestInterceptor.builder(brave).build())
                .addInterceptorFirst(BraveHttpResponseInterceptor.builder(brave).build())
                .setConnectionManager(httpClientConnectionManager)
                .setRoutePlanner(routePlanner)
                .build();
    }
}
