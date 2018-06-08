/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.as2.outbound;

import brave.Span;
import brave.Tracer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.lang.TimestampException;
import no.difi.oxalis.api.model.Direction;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.as2.api.MessageIdGenerator;
import no.difi.oxalis.as2.code.As2Header;
import no.difi.oxalis.as2.code.MdnHeader;
import no.difi.oxalis.as2.model.As2DispositionNotificationOptions;
import no.difi.oxalis.as2.model.Mic;
import no.difi.oxalis.as2.util.*;
import no.difi.oxalis.commons.bouncycastle.BCHelper;
import no.difi.oxalis.commons.security.CertificateUtils;
import no.difi.oxalis.commons.tracing.Traceable;
import no.difi.vefa.peppol.common.model.Digest;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    private final String notificationAddress;

    private final MessageIdGenerator messageIdGenerator;

    /**
     * Identifier from sender's certificate used during transmission in "AS2-From" header.
     */
    private final String fromIdentifier;

    private TransmissionRequest transmissionRequest;

    private TransmissionIdentifier transmissionIdentifier;

    private Span root;

    private Digest outboundMic;

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
                            @Named("as2-notification") String notificationAddress, MessageIdGenerator messageIdGenerator,
                            Tracer tracer) {
        super(tracer);
        this.httpClientProvider = httpClientProvider;
        this.sMimeMessageFactory = sMimeMessageFactory;
        this.timestampProvider = timestampProvider;
        this.notificationAddress = notificationAddress;
        this.messageIdGenerator = messageIdGenerator;

        // Establishes our AS2 System Identifier based upon the contents of the CN= field of the certificate
        this.fromIdentifier = CertificateUtils.extractCommonName(certificate);
    }

    public TransmissionResponse send(TransmissionRequest transmissionRequest, Span root)
            throws OxalisTransmissionException {
        this.transmissionRequest = transmissionRequest;

        this.root = tracer.newChild(root.context()).name("Send AS2 message").start();
        try {
            return sendHttpRequest(prepareHttpRequest());
        } catch (OxalisTransmissionException e) {
            this.root.tag("exception", e.getMessage());
            throw e;
        } finally {
            root.finish();
        }
    }

    @SuppressWarnings("unchecked")
    protected HttpPost prepareHttpRequest() throws OxalisTransmissionException {
        Span span = tracer.newChild(root.context()).name("request").start();
        try {
            // Create the body part of the MIME message containing our content to be transmitted.
            MimeBodyPart mimeBodyPart = MimeMessageHelper
                    .createMimeBodyPart(transmissionRequest.getPayload(), "application/xml");

            // Digest method to use.
            SMimeDigestMethod digestMethod = SMimeDigestMethod.findByTransportProfile(
                    transmissionRequest.getEndpoint().getTransportProfile());

            outboundMic = MimeMessageHelper.calculateMic(mimeBodyPart, digestMethod);
            span.tag("mic", outboundMic.toString());
            span.tag("endpoint url", transmissionRequest.getEndpoint().getAddress().toString());

            // Create Message-Id
            String messageId = messageIdGenerator.generate(transmissionRequest);

            if (!MessageIdUtil.verify(messageId))
                throw new OxalisTransmissionException("Invalid Message-ID '" + messageId + "' generated.");

            span.tag("message-id", messageId);
            transmissionIdentifier = TransmissionIdentifier.fromHeader(messageId);

            // Create a complete S/MIME message using the body part containing our content as the
            // signed part of the S/MIME message.
            MimeMessage signedMimeMessage = sMimeMessageFactory
                    .createSignedMimeMessage(mimeBodyPart, digestMethod);
            // .createSignedMimeMessageNew(mimeBodyPart, outboundMic, digestMethod);

            // Get all headers in S/MIME message.
            Map<String, String> headers = ((List<javax.mail.Header>) Collections.list(signedMimeMessage.getAllHeaders())).stream()
                    .collect(Collectors.toMap(javax.mail.Header::getName, h -> h.getValue().replace("\r\n\t", "")));

            // Clear headers in MIME content
            for (String name : headers.keySet())
                signedMimeMessage.removeHeader(name);

            // Initiate POST request
            HttpPost httpPost = new HttpPost(transmissionRequest.getEndpoint().getAddress());

            // Inserts the S/MIME message to be posted.
            httpPost.setEntity(new InputStreamEntity(signedMimeMessage.getInputStream()));

            // Set all headers.
            httpPost.addHeader(As2Header.MESSAGE_ID, messageId);
            httpPost.addHeader(As2Header.MIME_VERSION, headers.get(As2Header.MIME_VERSION));
            httpPost.addHeader(As2Header.CONTENT_TYPE, headers.get(As2Header.CONTENT_TYPE));
            httpPost.addHeader(As2Header.AS2_FROM, fromIdentifier);
            httpPost.setHeader(As2Header.AS2_TO, CertificateUtils.extractCommonName(
                    transmissionRequest.getEndpoint().getCertificate()));
            httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_TO, notificationAddress);
            httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS,
                    As2DispositionNotificationOptions.getDefault(digestMethod).toString());
            httpPost.addHeader(As2Header.AS2_VERSION, As2Header.VERSION);
            httpPost.addHeader(As2Header.SUBJECT, "AS2 message from Oxalis");
            httpPost.addHeader(As2Header.DATE, As2DateUtil.RFC822.format(new Date()));

            return httpPost;
        } catch (MessagingException | IOException e) {
            throw new OxalisTransmissionException("Unexpected error during preparation of AS2 message.", e);
        } finally {
            span.finish();
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
            span.tag("exception", String.valueOf(e.getMessage()));
            throw new OxalisTransmissionException(transmissionRequest.getEndpoint().getAddress(), e);
        } finally {
            span.finish();
        }
    }

    protected TransmissionResponse handleResponse(CloseableHttpResponse closeableHttpResponse)
            throws OxalisTransmissionException {
        Span span = tracer.newChild(root.context()).name("response").start();
        try (CloseableHttpResponse response = closeableHttpResponse) {
            span.tag("code", String.valueOf(response.getStatusLine().getStatusCode()));

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                LOGGER.error("AS2 HTTP POST expected HTTP OK, but got : {} from {}",
                        response.getStatusLine().getStatusCode(), transmissionRequest.getEndpoint().getAddress());

                // Throws exception
                handleFailedRequest(response);
            }

            // handle normal HTTP OK response
            LOGGER.debug("AS2 transmission to {} returned HTTP OK, verify MDN response",
                    transmissionRequest.getEndpoint().getAddress());

            Header contentTypeHeader = response.getFirstHeader("Content-Type");
            if (contentTypeHeader == null)
                throw new OxalisTransmissionException("No Content-Type header in response, probably a server error.");

            // Read MIME Message
            MimeMessage mimeMessage = MimeMessageHelper.parseMultipart(
                    response.getEntity().getContent(), contentTypeHeader.getValue());

            // Add headers to MIME Message
            for (Header header : response.getAllHeaders())
                mimeMessage.addHeader(header.getName(), header.getValue());

            SMimeReader sMimeReader = new SMimeReader(mimeMessage);

            // Timestamp of reception of MDN
            Timestamp t3 = timestampProvider.generate(sMimeReader.getSignature(), Direction.OUT);

            // Extract signed digest and digest algorithm
            SMimeDigestMethod digestMethod = sMimeReader.getDigestMethod();

            // Preparing calculation of digest
            MessageDigest messageDigest = BCHelper.getMessageDigest(digestMethod.getIdentifier());
            InputStream digestInputStream = new DigestInputStream(sMimeReader.getBodyInputStream(), messageDigest);

            // Reading report
            MimeMultipart mimeMultipart = new MimeMultipart(
                    new ByteArrayDataSource(digestInputStream, mimeMessage.getContentType()));

            // Create digest object
            Digest digest = Digest.of(digestMethod.getDigestMethod(), messageDigest.digest());

            // Verify signature
            /*
            X509Certificate certificate = SMimeBC.verifySignature(
                    ImmutableMap.of(digestMethod.getOid(), digest.getValue()),
                    sMimeReader.getSignature()
            );
            */

            // verify the signature of the MDN, we warn about dodgy signatures
            SignedMimeMessage signedMimeMessage;
            try {
                signedMimeMessage = new SignedMimeMessage(mimeMessage);
            } catch (Exception e) {
                throw new OxalisTransmissionException("Unable to parse signature content.", e);
            }

            X509Certificate certificate = signedMimeMessage.getSignersX509Certificate();

            // Verify if the certificate used by the receiving Access Point in
            // the response message does not match its certificate published by the SMP
            if (!transmissionRequest.getEndpoint().getCertificate().equals(certificate))
                throw new OxalisTransmissionException(String.format(
                        "Certificate in MDN ('%s') does not match certificate from SMP ('%s').",
                        certificate.getSubjectX500Principal().getName(),
                        transmissionRequest.getEndpoint().getCertificate().getSubjectX500Principal().getName()));

            LOGGER.debug("MDN signature was verified for : " + certificate.getSubjectDN().toString());

            // Verifies the actual MDN
            MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
            String msg = mdnMimeMessageInspector.getPlainTextPartAsText();

            if (!mdnMimeMessageInspector.isOkOrWarning(new Mic(outboundMic))) {
                LOGGER.error("AS2 transmission failed with some error message '{}'.", msg);
                throw new OxalisTransmissionException(String.format("AS2 transmission failed : %s", msg));
            }

            // Read structured content
            MimeBodyPart mimeBodyPart = (MimeBodyPart) mdnMimeMessageInspector.getMessageDispositionNotificationPart();
            InternetHeaders internetHeaders = new InternetHeaders((InputStream) mimeBodyPart.getContent());

            // Fetch timestamp if set
            Date date = t3.getDate();
            if (internetHeaders.getHeader(MdnHeader.DATE) != null)
                date = As2DateUtil.RFC822.parse(internetHeaders.getHeader(MdnHeader.DATE)[0]);

            // Return TransmissionResponse
            return new As2TransmissionResponse(transmissionIdentifier, transmissionRequest,
                    outboundMic, MimeMessageHelper.toBytes(mimeMessage), t3, date);
        } catch (TimestampException | IOException e) {
            throw new OxalisTransmissionException(e.getMessage(), e);
        } catch (NoSuchAlgorithmException | MessagingException e) {
            throw new OxalisTransmissionException(String.format("Unable to parse received MDN: %s", e.getMessage()), e);
        } finally {
            span.finish();
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
