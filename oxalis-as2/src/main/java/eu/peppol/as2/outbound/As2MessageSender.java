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
import com.google.inject.Inject;
import com.google.inject.Provider;
import eu.peppol.as2.model.As2DispositionNotificationOptions;
import eu.peppol.as2.util.As2Header;
import eu.peppol.as2.model.Mic;
import eu.peppol.as2.util.*;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.lang.TimestampException;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.commons.security.CertificateUtils;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.commons.tracing.Traceable;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.SSLHandshakeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Not thread safe implementation of sender, which sends messages using the AS2 protocol.
 * Stores the outbound MIC for verification against the mic received from the MDN later.
 *
 * @author steinar
 * @author thore
 * @author erlend
 */
class As2MessageSender extends Traceable {

    private static final Logger LOGGER = LoggerFactory.getLogger(As2MessageSender.class);

    /**
     * Provider of HTTP clients.
     */
    private final Provider<CloseableHttpClient> httpClientProvider;

    private final SMimeMessageFactory sMimeMessageFactory;

    /**
     * Timestamp provider used to create timestamp "t3" (time of reception of transport specific receipt, MDN).
     */
    private final TimestampProvider timestampProvider;

    /**
     * Identifier from sender's certificate used during transmission in "AS2-From" header.
     */
    private final String fromIdentifier;

    private TransmissionRequest transmissionRequest;

    private Span root;

    private Mic outboundMic;

    /**
     * Constructor expecting resources needed to perform transmission using AS2. All task required to be done once for
     * all requests using this instance is done here.
     *
     * @param httpClientProvider  Provider of HTTP clients.
     * @param certificate         Certificate of sender.
     * @param sMimeMessageFactory Factory prepared to create S/MIME messages using our private key.
     * @param timestampProvider   Provider used to fetch timestamps.
     * @param tracer              Tracing tool.
     */
    @Inject
    public As2MessageSender(Provider<CloseableHttpClient> httpClientProvider, X509Certificate certificate,
                            SMimeMessageFactory sMimeMessageFactory, TimestampProvider timestampProvider,
                            Tracer tracer) {
        super(tracer);
        this.httpClientProvider = httpClientProvider;
        this.sMimeMessageFactory = sMimeMessageFactory;
        this.timestampProvider = timestampProvider;

        // Establishes our AS2 System Identifier based upon the contents of the CN= field of the certificate
        this.fromIdentifier = CertificateUtils.extractCommonName(certificate);
    }

    public TransmissionResponse send(TransmissionRequest transmissionRequest, Span root)
            throws OxalisTransmissionException {
        this.transmissionRequest = transmissionRequest;

        try (Span span = tracer.newChild(root.context()).name("Send AS2 message").start()) {
            this.root = span;
            try {
                return sendHttpRequest(prepareHttpRequest());
            } catch (OxalisTransmissionException e) {
                this.root.tag("exception", e.getMessage());
                throw e;
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected HttpPost prepareHttpRequest() throws OxalisTransmissionException {
        try (Span span = tracer.newChild(root.context()).name("request").start()) {
            try {
                if (transmissionRequest.getMessageId() == null) {
                    throw new NullPointerException("MessageId required argument");
                }

                final HttpPost httpPost;

                // Create the body part of the MIME message containing our content to be transmitted.
                MimeBodyPart mimeBodyPart = MimeMessageHelper
                        .createMimeBodyPart(transmissionRequest.getPayload(), "application/xml");

                outboundMic = MimeMessageHelper.calculateMic(mimeBodyPart);
                LOGGER.debug("Outbound MIC is : {}", outboundMic);
                span.tag("mic", outboundMic.toString());
                span.tag("endpoint url", transmissionRequest.getEndpoint().getAddress().toString());

                // Create a complete S/MIME message using the body part containing our content as the
                // signed part of the S/MIME message.
                MimeMessage signedMimeMessage = sMimeMessageFactory.createSignedMimeMessage(mimeBodyPart);

                // Initiate POST request
                httpPost = new HttpPost(transmissionRequest.getEndpoint().getAddress());

                // Get all headers in S/MIME message.
                List<javax.mail.Header> headers = Collections.list(signedMimeMessage.getAllHeaders());

                List<String> headerNames = headers.stream()
                        // Tag for tracing.
                        .peek(h -> span.tag(h.getName(), h.getValue()))
                                // Add headers to httpPost object (remove new lines according to HTTP 1.1).
                        .peek(h -> httpPost.addHeader(h.getName(), h.getValue().replace("\r\n\t", "")))
                                // Collect header names....
                        .map(javax.mail.Header::getName)
                                // ... in a list.
                        .collect(Collectors.toList());

                // Write content to OutputStream without headers.
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                signedMimeMessage.writeTo(byteArrayOutputStream, headerNames.toArray(new String[headerNames.size()]));

                // Inserts the S/MIME message to be posted. Make sure we pass the same content type as the
                // SignedMimeMessage, it'll end up as content-type HTTP header
                httpPost.setEntity(new ByteArrayEntity(byteArrayOutputStream.toByteArray()));

                // Detect certificate "Common Name" (CN) to be used as "AS2-To" value. Use "unknown" if no certificate
                // is found.
                String receiverName = "unknown";
                if (transmissionRequest.getEndpoint().getCertificate() != null)
                    receiverName = CertificateUtils.extractCommonName(
                            transmissionRequest.getEndpoint().getCertificate());

                // Set all headers specific to AS2 (not MIME).
                httpPost.addHeader(As2Header.AS2_FROM, fromIdentifier);
                httpPost.setHeader(As2Header.AS2_TO, receiverName);
                httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_TO, "not.in.use@difi.no");
                httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS,
                        As2DispositionNotificationOptions.getDefault().toString());
                httpPost.addHeader(As2Header.AS2_VERSION, As2Header.VERSION);
                httpPost.addHeader(As2Header.SUBJECT, "AS2 message from OXALIS");
                httpPost.addHeader(As2Header.DATE, As2DateUtil.format(new Date()));

                return httpPost;
            } catch (MessagingException | IOException e) {
                throw new OxalisTransmissionException("Unable to stream S/MIME message into byte array output stream");
            }
        }
    }

    protected TransmissionResponse sendHttpRequest(HttpPost httpPost) throws OxalisTransmissionException {
        Span span = tracer.newChild(root.context()).name("execute").start();
        try (CloseableHttpClient httpClient = httpClientProvider.get()) {

            CloseableHttpResponse response = httpClient.execute(httpPost);

            span.finish();

            return handleResponse(response);
        } catch (HttpHostConnectException e) {
            span.tag("exception", e.getMessage());
            throw new OxalisTransmissionException("Receiving server does not seem to be running.",
                    transmissionRequest.getEndpoint().getAddress(), e);
        } catch (SSLHandshakeException e) {
            span.tag("exception", e.getMessage());
            throw new OxalisTransmissionException("Possible invalid SSL Certificate at the other end.",
                    transmissionRequest.getEndpoint().getAddress(), e);
        } catch (IOException e) {
            span.tag("exception", e.getMessage());
            throw new OxalisTransmissionException(transmissionRequest.getEndpoint().getAddress(), e);
        } finally {
            span.finish();
        }
    }

    protected TransmissionResponse handleResponse(CloseableHttpResponse closeableHttpResponse)
            throws OxalisTransmissionException {
        try (Span span = tracer.newChild(root.context()).name("response").start()) {
            try (CloseableHttpResponse response = closeableHttpResponse) {
                span.tag("code", String.valueOf(response.getStatusLine().getStatusCode()));

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    LOGGER.error("AS2 HTTP POST expected HTTP OK, but got : {} from {}",
                            response.getStatusLine().getStatusCode(), transmissionRequest.getEndpoint().getAddress());

                    // Throws exception
                    handleFailedRequest(response);
                }

                // handle normal HTTP OK response
                LOGGER.debug("AS2 transmission {} to {} returned HTTP OK, verify MDN response",
                        transmissionRequest.getMessageId(), transmissionRequest.getEndpoint().getAddress());

                // Prepare calculation of message digest.
                MessageDigest digest = MessageDigest.getInstance("sha1", BouncyCastleProvider.PROVIDER_NAME);
                DigestInputStream digestInputStream = new DigestInputStream(response.getEntity().getContent(), digest);

                Header contentTypeHeader = response.getFirstHeader("Content-Type");
                if (contentTypeHeader == null)
                    throw new OxalisTransmissionException("No Content-Type header in response, probably a server error.");

                MimeMessage mimeMessage = MimeMessageHelper.parseMultipart(digestInputStream, contentTypeHeader.getValue());

                // verify the signature of the MDN, we warn about dodgy signatures
                SignedMimeMessage signedMimeMessage = new SignedMimeMessage(mimeMessage);
                X509Certificate cert = signedMimeMessage.getSignersX509Certificate();

                // Verify if the certificate used by the receiving Access Point in
                // the response message does not match its certificate published by the SMP
                if (transmissionRequest.getEndpoint().getCertificate() != null)
                    if (!transmissionRequest.getEndpoint().getCertificate().equals(cert))
                        throw new OxalisTransmissionException(
                                "Common name in certificate from SMP does not match common name in AP certificate");

                LOGGER.debug("MDN signature was verified for : " + cert.getSubjectDN().toString());

                // Verifies the actual MDN
                MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
                String msg = mdnMimeMessageInspector.getPlainTextPartAsText();

                if (!mdnMimeMessageInspector.isOkOrWarning(outboundMic)) {
                    LOGGER.error("AS2 transmission failed with some error message '{}'.", msg);
                    throw new OxalisTransmissionException(String.format("AS2 transmission failed : %s", msg));
                }

                Timestamp t3 = timestampProvider.generate(outboundMic.toString().getBytes());

                return new As2TransmissionResponse(transmissionRequest,
                        MimeMessageHelper.toBytes(mimeMessage), t3.getDate());
            } catch (TimestampException | IOException e) {
                throw new OxalisTransmissionException(e.getMessage(), e);
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new OxalisTransmissionException("Unable to parse received content.", e);
            }
        }
    }

    protected void handleFailedRequest(HttpResponse response) throws OxalisTransmissionException {
        HttpEntity entity = response.getEntity();   // Any results?
        try {
            if (entity == null) {
                // No content returned
                throw new OxalisTransmissionException(
                        String.format("Request failed with rc=%s, no content returned in HTTP response",
                                response.getStatusLine().getStatusCode()));
            } else {
                String contents = EntityUtils.toString(entity);
                throw new OxalisTransmissionException(
                        String.format("Request failed with rc=%s, contents received (%s characters): %s",
                                response.getStatusLine().getStatusCode(), contents.trim().length(), contents));
            }
        } catch (IOException e) {
            throw new OxalisTransmissionException(
                    String.format("Request failed with rc=%s, ERROR while retrieving the contents of the response: %s",
                            response.getStatusLine().getStatusCode(), e.getMessage()), e);
        }
    }
}