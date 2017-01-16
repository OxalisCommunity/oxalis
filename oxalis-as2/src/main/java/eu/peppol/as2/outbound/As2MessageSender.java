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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.as2.outbound;

import brave.Span;
import brave.Tracer;
import com.google.inject.Inject;
import eu.peppol.as2.*;
import eu.peppol.as2.lang.InvalidAs2SystemIdentifierException;
import eu.peppol.identifier.MessageId;
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.smp.PeppolEndpointData;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampService;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.util.EntityUtils;
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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Thread safe implementation of a {@link MessageSender}, which sends messages using the AS2 protocol.
 * Stores the outbound MIC for verification against the mic received from the MDN later.
 *
 * @author steinar
 * @author thore
 */
class As2MessageSender implements MessageSender {

    private static final Logger log = LoggerFactory.getLogger(As2MessageSender.class);

    private final PoolingHttpClientConnectionManager httpClientConnectionManager;

    private final KeystoreManager keystoreManager;

    private final SMimeMessageFactory sMimeMessageFactory;

    private final TimestampService timestampService;

    private final Tracer tracer;

    private PeppolAs2SystemIdentifier as2SystemIdentifierOfSender;

    @Inject
    public As2MessageSender(KeystoreManager keystoreManager, TimestampService timestampService, Tracer tracer) {
        this.keystoreManager = keystoreManager;
        this.sMimeMessageFactory = new SMimeMessageFactory(keystoreManager.getOurPrivateKey(), keystoreManager.getOurCertificate());
        this.timestampService = timestampService;
        this.tracer = tracer;

        // Establishes our AS2 System Identifier based upon the contents of the CN= field of the certificate
        as2SystemIdentifierOfSender = getAs2SystemIdentifierForSender();

        // Setting up Http Connection pool.
        httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        httpClientConnectionManager.setDefaultMaxPerRoute(10);
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
    public TransmissionResponse send(TransmissionRequest transmissionRequest, Span root) throws OxalisTransmissionException {

        PeppolEndpointData endpointAddress = transmissionRequest.getEndpointAddress();
        if (endpointAddress.getCommonName() == null)
            throw new IllegalStateException("Must supply the X.509 common name (AS2 System Identifier) of the end point for AS2 protocol");

        try (Span span = tracer.newChild(root.context()).name("Send AS2 message").start()) {
            SendResult sendResult = perform(
                    transmissionRequest.getPayload(),
                    transmissionRequest.getHeader(),
                    transmissionRequest.getMessageId(),
                    transmissionRequest.getEndpointAddress(),
                    span
            );

            return new As2TransmissionResponse(
                    sendResult.messageId,
                    transmissionRequest.getPeppolStandardBusinessHeader(),
                    endpointAddress.getUrl(),
                    endpointAddress.getTransportProfile(),
                    endpointAddress.getCommonName(),
                    sendResult.signedMimeMdnBytes
            );
        }
    }

    /**
     * This is the work horse method of this class, responsible for the actual http transmission.
     *
     * @throws OxalisTransmissionException
     */
    SendResult perform(InputStream inputStream,
                       no.difi.vefa.peppol.common.model.Header header,
                       MessageId messageId,
                       PeppolEndpointData peppolEndpointData,
                       Span root) throws OxalisTransmissionException {

        final String endpointAddress;
        final Mic mic;
        final HttpPost httpPost;

        try (Span span = tracer.newChild(root.context()).name("request").start()) {
            try {
                if (peppolEndpointData.getCommonName() == null) {
                    throw new IllegalArgumentException("No common name in EndPoint object. " + peppolEndpointData);
                }
                if (messageId == null) {
                    throw new NullPointerException("MessageId required argument");
                }

                MimeMessage signedMimeMessage;
                try {
                    MimeBodyPart mimeBodyPart = MimeMessageHelper.createMimeBodyPart(inputStream, new MimeType("application/xml"));
                    mic = MimeMessageHelper.calculateMic(mimeBodyPart);
                    log.debug("Outbound MIC is : " + mic.toString());
                    span.tag("mic", mic.toString());
                    signedMimeMessage = sMimeMessageFactory.createSignedMimeMessage(mimeBodyPart);
                } catch (MimeTypeParseException e) {
                    throw new IllegalStateException("Problems with MIME types: " + e.getMessage(), e);
                }


                endpointAddress = peppolEndpointData.getUrl().toString();
                httpPost = new HttpPost(endpointAddress);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    signedMimeMessage.writeTo(byteArrayOutputStream);

                } catch (Exception e) {
                    throw new IllegalStateException("Unable to stream S/MIME message into byte array output stream");
                }

                httpPost.addHeader(As2Header.AS2_FROM.getHttpHeaderName(), as2SystemIdentifierOfSender.toString());
                try {
                    httpPost.setHeader(As2Header.AS2_TO.getHttpHeaderName(), PeppolAs2SystemIdentifier.valueOf(peppolEndpointData.getCommonName()).toString());
                } catch (InvalidAs2SystemIdentifierException e) {
                    throw new IllegalArgumentException(
                            String.format("Unable to create valid AS2 System Identifier for receiving end point: %s", peppolEndpointData));
                }

                httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_TO.getHttpHeaderName(), "not.in.use@difi.no");
                httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(), As2DispositionNotificationOptions.getDefault().toString());
                httpPost.addHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
                httpPost.addHeader(As2Header.SUBJECT.getHttpHeaderName(), "AS2 message from OXALIS");

                httpPost.addHeader(As2Header.MESSAGE_ID.getHttpHeaderName(), messageId.stringValue());
                span.tag("message id", messageId.stringValue());

                httpPost.addHeader(As2Header.DATE.getHttpHeaderName(), As2DateUtil.format(new Date()));

                // Inserts the S/MIME message to be posted.
                // Make sure we pass the same content type as the SignedMimeMessage, it'll end up as content-type HTTP header
                try {
                    String contentType = signedMimeMessage.getContentType();
                    ContentType ct = ContentType.create(contentType);
                    httpPost.setEntity(new ByteArrayEntity(byteArrayOutputStream.toByteArray(), ct));
                } catch (Exception ex) {
                    throw new IllegalStateException("Unable to set request header content type : " + ex.getMessage());
                }
            } catch (RuntimeException e) {
                span.tag("exception", e.getMessage());
                throw e;
            }
        }

        CloseableHttpResponse postResponse; // EXECUTE !!!!
        Timestamp t3;
        try (Span span = tracer.newChild(root.context()).name("execute").start()) {
            try {
                span.tag("sender", header.getSender().toString());
                span.tag("recipient", header.getReceiver().toString());
                span.tag("endpoint url", endpointAddress);

                CloseableHttpClient httpClient = createCloseableHttpClient();

                log.debug("Sending AS2 from {} to {} at {}.", header.getSender().getIdentifier(), header.getReceiver().getIdentifier(), endpointAddress);
                postResponse = httpClient.execute(httpPost);

                t3 = timestampService.generate(mic.toString().getBytes());
            } catch (HttpHostConnectException e) {
                span.tag("exception", e.getMessage());
                throw new OxalisTransmissionException("Oxalis server does not seem to be running.", peppolEndpointData.getUrl(), e);
            } catch (SSLHandshakeException e) {
                span.tag("exception", e.getMessage());
                throw new OxalisTransmissionException("Possible invalid SSL Certificate at the other end.", peppolEndpointData.getUrl(), e);
            } catch (Exception e) {
                span.tag("exception", e.getMessage());
                throw new OxalisTransmissionException(peppolEndpointData.getUrl(), e);
            }
        }

        try (Span span = tracer.newChild(root.context()).name("response").start()) {
            try {
                span.tag("code", String.valueOf(postResponse.getStatusLine().getStatusCode()));

                if (postResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    log.error("AS2 HTTP POST expected HTTP OK, but got : {} from {}", postResponse.getStatusLine().getStatusCode(), endpointAddress);
                    throw handleFailedRequest(postResponse);
                }

                // handle normal HTTP OK response
                log.debug("AS2 transmission {} to {} returned HTTP OK, verify MDN response", messageId, endpointAddress);
                MimeMessage signedMimeMDN = handleTheHttpResponse(mic, postResponse, peppolEndpointData);

                return new SendResult(messageId, MimeMessageHelper.toBytes(signedMimeMDN));
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
     * @param postResponse the http response to be decoded as MDN
     */
    MimeMessage handleTheHttpResponse(Mic outboundMic, CloseableHttpResponse postResponse, PeppolEndpointData peppolEndpointData) {
        try {

            HttpEntity entity = postResponse.getEntity();   // Any textual results?
            if (entity == null) {
                throw new IllegalStateException("No contents in HTTP response with rc=" + postResponse.getStatusLine().getStatusCode());
            }

            String contents = EntityUtils.toString(entity);

            if (log.isDebugEnabled()) {
                log.debug("HTTP-headers:");
                Header[] allHeaders = postResponse.getAllHeaders();
                for (Header header : allHeaders) {
                    log.debug("" + header.getName() + ": " + header.getValue());
                }
                log.debug("Contents:\n" + contents);
                log.debug("---------------------------");
            }

            Header contentTypeHeader = postResponse.getFirstHeader("Content-Type");
            if (contentTypeHeader == null) {
                throw new IllegalStateException("No Content-Type header in response, probably a server error");
            }
            String contentType = contentTypeHeader.getValue();

            MimeMessage mimeMessage;
            try {
                mimeMessage = MimeMessageHelper.parseMultipart(contents, new MimeType(contentType));

                dumpMdnToLogger(mimeMessage);

            } catch (MimeTypeParseException e) {
                throw new IllegalStateException("Invalid Content-Type header");
            }

            // verify the signature of the MDN, we warn about dodgy signatures
            try {
                SignedMimeMessage signedMimeMessage = new SignedMimeMessage(mimeMessage);
                X509Certificate cert = signedMimeMessage.getSignersX509Certificate();
                cert.checkValidity();

                // Verify if the certificate used by the receiving Access Point in
                // the response message does not match its certificate published by the SMP
                if (peppolEndpointData.getCommonName() == null || !CommonName.of(cert).equals(peppolEndpointData.getCommonName())) {
                    throw new CertificateException("Common name in certificate from SMP does not match common name in AP certificate");
                }

                log.debug("MDN signature was verfied for : " + cert.getSubjectDN().toString());
            } catch (Exception ex) {
                log.warn("Exception when verifying MDN signature : " + ex.getMessage());
            }

            // Verifies the actual MDN
            MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
            String msg = mdnMimeMessageInspector.getPlainTextPartAsText();

            if (mdnMimeMessageInspector.isOkOrWarning(outboundMic)) {
                // TODO: save the native transport evidence.
                return mimeMessage;
            } else {
                log.error("AS2 transmission failed with some error message, msg :{}", msg);
                log.error(contents);
                throw new IllegalStateException("AS2 transmission failed : " + msg);
            }

        } catch (IOException e) {
            throw new IllegalStateException("Unable to obtain the contents of the response: " + e.getMessage(), e);
        } finally {
            try {
                postResponse.close();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to close http connection: " + e.getMessage(), e);
            }
        }
    }

    private void dumpMdnToLogger(MimeMessage mimeMessage) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("======================================================");
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mimeMessage.writeTo(baos);
                log.debug(baos.toString());
            } catch (MessagingException e) {
                throw new IllegalStateException("Unable to print mime message");
            }
            log.debug("======================================================");
        }
    }

    IllegalStateException handleFailedRequest(CloseableHttpResponse postResponse) {
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

    private PeppolAs2SystemIdentifier getAs2SystemIdentifierForSender() {
        X509Certificate ourCertificate = keystoreManager.getOurCertificate();
        try {
            // TODO: replace this with method in KeystoreManager
            return PeppolAs2SystemIdentifier.valueOf(CommonName.of(ourCertificate));
        } catch (InvalidAs2SystemIdentifierException e) {
            throw new IllegalStateException("AS2 System Identifier could not be obtained from " + ourCertificate.getSubjectX500Principal(), e);
        }
    }


    CloseableHttpClient createCloseableHttpClient() {

        // "SSLv3" is disabled by default : http://www.apache.org/dist/httpcomponents/httpclient/RELEASE_NOTES-4.3.x.txt
        SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());

        return HttpClients.custom()
                .setConnectionManager(httpClientConnectionManager)
                .setRoutePlanner(routePlanner)
                .build();
    }
}
