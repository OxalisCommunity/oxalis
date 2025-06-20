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

package network.oxalis.as2.outbound;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.identifier.MessageIdGenerator;
import network.oxalis.api.lang.OxalisSecurityException;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.lang.TimestampException;
import network.oxalis.api.model.Direction;
import network.oxalis.api.model.TransmissionIdentifier;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.api.timestamp.Timestamp;
import network.oxalis.api.timestamp.TimestampProvider;
import network.oxalis.as2.code.As2Header;
import network.oxalis.as2.code.MdnHeader;
import network.oxalis.as2.lang.OxalisAs2Exception;
import network.oxalis.as2.model.As2DispositionNotificationOptions;
import network.oxalis.as2.model.Mic;
import network.oxalis.as2.util.*;
import network.oxalis.commons.security.CertificateUtils;
import network.oxalis.commons.tracing.Traceable;
import network.oxalis.vefa.peppol.common.model.Digest;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.BasicHttpContext;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Not thread safe implementation of sender, which sends messages using the AS2 protocol.
 * Stores the outbound MIC for verification against the mic received from the MDN later.
 *
 * @author steinar
 * @author thore
 * @author erlend
 */
@Slf4j
class As2MessageSender extends Traceable {

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

    public TransmissionResponse send(TransmissionRequest transmissionRequest)
            throws OxalisTransmissionException {
        this.transmissionRequest = transmissionRequest;

        Span root = tracer.spanBuilder("Send AS2 message").startSpan();
        try {
            return sendHttpRequest(prepareHttpRequest());
        } catch (OxalisTransmissionException e) {
            root.setAttribute("exception", e.getMessage());
            throw e;
        } finally {
            root.end();
        }
    }

    @SuppressWarnings("unchecked")
    protected HttpPost prepareHttpRequest() throws OxalisTransmissionException {
        Span span = tracer.spanBuilder("request").startSpan();
        try {
            // Create the body part of the MIME message containing our content to be transmitted.
            MimeBodyPart mimeBodyPart = MimeMessageHelper
                    .createMimeBodyPart(transmissionRequest.getPayload(), "application/xml");

            // Digest method to use.
            SMimeDigestMethod digestMethod = SMimeDigestMethod.findByTransportProfile(
                    transmissionRequest.getEndpoint().getTransportProfile());

            outboundMic = MimeMessageHelper.calculateMic(mimeBodyPart, digestMethod);
            span.setAttribute("mic", outboundMic.toString());
            span.setAttribute("endpoint url", transmissionRequest.getEndpoint().getAddress().toString());

            // Create Message-Id
            String messageId = messageIdGenerator.generate(transmissionRequest);

            if (!MessageIdUtil.verify(messageId))
                throw new OxalisTransmissionException("Invalid Message-ID '" + messageId + "' generated.");

            span.setAttribute("message-id", messageId);
            transmissionIdentifier = TransmissionIdentifier.fromHeader(messageId);

            // Create a complete S/MIME message using the body part containing our content as the
            // signed part of the S/MIME message.
            MimeMessage signedMimeMessage = sMimeMessageFactory
                    .createSignedMimeMessage(mimeBodyPart, digestMethod);
            // .createSignedMimeMessageNew(mimeBodyPart, outboundMic, digestMethod);

            //try (OutputStream outputStream = Files.newOutputStream(Paths.get(UUID.randomUUID().toString()))) {
            //    signedMimeMessage.writeTo(outputStream);
            //}

            // Get all headers in S/MIME message.
            Map<String, String> headers = ((List<jakarta.mail.Header>) Collections.list(signedMimeMessage.getAllHeaders())).stream()
                    .collect(Collectors.toMap(jakarta.mail.Header::getName, h -> h.getValue().replace("\r\n\t", "")));

            // Clear headers in MIME content
            for (String name : headers.keySet())
                signedMimeMessage.removeHeader(name);

            // Initiate POST request
            HttpPost httpPost = new HttpPost(transmissionRequest.getEndpoint().getAddress());

            // Inserts the S/MIME message to be posted.
            HttpEntity httpEntity = new ByteArrayEntity(ByteStreams.toByteArray(signedMimeMessage.getInputStream()), ContentType.DEFAULT_BINARY);
            httpPost.setEntity(httpEntity);

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
            span.end();
        }
    }

    protected TransmissionResponse sendHttpRequest(HttpPost httpPost) throws OxalisTransmissionException {
        Span span = tracer.spanBuilder("execute").startSpan();
        try (CloseableHttpClient httpClient = httpClientProvider.get()) {
            BasicHttpContext basicHttpContext = new BasicHttpContext();
            basicHttpContext.setAttribute(As2MessageSender.class.getName() + ".parentSpanContext", span.getSpanContext());

            CloseableHttpResponse response = httpClient.execute(httpPost, basicHttpContext);

            return handleResponse(response);
        } catch (SocketTimeoutException e) {
            span.setAttribute("exception", String.valueOf(e.getMessage()));
            throw new OxalisTransmissionException("Receiving server has not sent anything back within SOCKET_TIMEOUT", transmissionRequest.getEndpoint().getAddress(), e);
        } catch (HttpHostConnectException e) {
            span.setAttribute("exception", e.getMessage());
            throw new OxalisTransmissionException("Receiving server does not seem to be running.",
                    transmissionRequest.getEndpoint().getAddress(), e);
        } catch (SSLHandshakeException e) {
            span.setAttribute("exception", e.getMessage());
            throw new OxalisTransmissionException("Possible invalid SSL Certificate at the other end.",
                    transmissionRequest.getEndpoint().getAddress(), e);
        } catch (IOException e) {
            span.setAttribute("exception", String.valueOf(e.getMessage()));
            throw new OxalisTransmissionException(transmissionRequest.getEndpoint().getAddress(), e);
        } finally {
            span.end();
        }
    }

    protected TransmissionResponse handleResponse(CloseableHttpResponse closeableHttpResponse)
            throws OxalisTransmissionException {
        Span span = tracer.spanBuilder("response").startSpan();
        try (CloseableHttpResponse response = closeableHttpResponse) {
            span.setAttribute("code", String.valueOf(response.getCode()));

            if (response.getCode() != HttpStatus.SC_OK) {
                log.error("AS2 HTTP POST expected HTTP OK, but got : {} from {}",
                        response.getCode(), transmissionRequest.getEndpoint().getAddress());

                // Throws exception
                handleFailedRequest(response);
            }

            // handle normal HTTP OK response
            log.debug("AS2 transmission to {} returned HTTP OK, verify MDN response",
                    transmissionRequest.getEndpoint().getAddress());

            // Verify existence of Content-Type
            if (!response.containsHeader("Content-Type"))
                throw new OxalisTransmissionException("No Content-Type header in response, probably a server error.");

            // Read MIME message
            try (InputStream contentStream = response.getEntity().getContent()) {
                MimeMessage mimeMessage = MimeMessageHelper.parse(
                        contentStream,
                        Stream.of(response.getHeaders()).map(Object::toString)
                );


                SignedMessage message;
                try {
                    message = SignedMessage.load(mimeMessage);
                    message.validate(transmissionRequest.getEndpoint().getCertificate());
                } catch (OxalisAs2Exception e) {
                    throw new OxalisTransmissionException("Unable to parse received MDN.", e);
                } catch (OxalisSecurityException | PeppolSecurityException e) {
                    throw new OxalisTransmissionException(
                            "Unable to verify content of MDN using certificate provided by SMP.", e);
                }

                // Timestamp of reception of MDN
                Timestamp t3 = timestampProvider.generate(message.getSignature(), Direction.OUT);

                // Verifies the actual MDN
                MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
                String msg = mdnMimeMessageInspector.getPlainTextPartAsText();

                if (!mdnMimeMessageInspector.isOkOrWarning(new Mic(outboundMic))) {
                    log.error("AS2 transmission failed with some error message '{}'.", msg);
                    throw new OxalisTransmissionException(String.format("AS2 transmission failed : %s", msg));
                }

                // Read structured content
                MimeBodyPart mimeBodyPart = (MimeBodyPart) mdnMimeMessageInspector.getMessageDispositionNotificationPart();
                InternetHeaders internetHeaders = new InternetHeaders((InputStream) mimeBodyPart.getContent());

                // Fetch timestamp if set
                Date date = t3.getDate();
                if (internetHeaders.getHeader(MdnHeader.DATE) != null) {
                    date = As2DateUtil.RFC822.parse(internetHeaders.getHeader(MdnHeader.DATE)[0]);
                }

                // Return TransmissionResponse
                return new As2TransmissionResponse(transmissionIdentifier, transmissionRequest,
                        outboundMic, MimeMessageHelper.toBytes(mimeMessage), t3, date);
            }
        } catch (TimestampException | IOException e) {
            throw new OxalisTransmissionException(e.getMessage(), e);
        } catch (NoSuchAlgorithmException | MessagingException e) {
            throw new OxalisTransmissionException(
                    String.format("Unable to parseOld received MDN: %s", e.getMessage()), e);
        } finally {
            span.end();
        }
    }

    protected void handleFailedRequest(HttpResponse response) throws OxalisTransmissionException {
        ClassicHttpResponse classicResponse = (ClassicHttpResponse) response;
        HttpEntity entity = classicResponse.getEntity();  // Any results?

        try {
            if (entity == null) {
                // No content returned
                throw new OxalisTransmissionException(
                        String.format("Request failed with rc=%s, no content returned in HTTP response",
                                response.getCode()));
            } else {
                String contents = EntityUtils.toString(entity);
                throw new OxalisTransmissionException(
                        String.format("Request failed with rc=%s, contents received (%s characters): %s",
                                response.getCode(), contents.trim().length(), contents));
            }
        } catch (IOException | ParseException e) {
            throw new OxalisTransmissionException(
                    String.format("Request failed with rc=%s, ERROR while retrieving the contents of the response: %s",
                            response.getCode(), e.getMessage()), e);
        }
    }
}
