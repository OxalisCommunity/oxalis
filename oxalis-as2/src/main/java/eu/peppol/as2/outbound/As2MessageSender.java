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
import eu.peppol.as2.evidence.As2RemWithMdnTransmissionEvidenceImpl;
import eu.peppol.as2.evidence.As2TransmissionEvidenceFactory;
import eu.peppol.as2.lang.InvalidAs2SystemIdentifierException;
import eu.peppol.identifier.MessageId;
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.smp.PeppolEndpointData;
import eu.peppol.util.TimeWatch;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.vefa.peppol.common.model.DocumentTypeIdentifier;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.difi.vefa.peppol.evidence.jaxb.receipt.TransmissionRole;
import no.difi.vefa.peppol.evidence.rem.EventCode;
import org.apache.commons.io.IOUtils;
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
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
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
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Thread safe implementation of a {@link MessageSender}, which sends messages using the AS2 protocol.
 * Stores the outbound MIC for verification against the mic received from the MDN later.
 *
 * @author steinar
 * @author thore
 */
public class As2MessageSender implements MessageSender {

    private static final Logger log = LoggerFactory.getLogger(As2MessageSender.class);

    private final KeystoreManager keystoreManager;

    private final As2TransmissionEvidenceFactory as2TransmissionEvidenceFactory;

    private final SMimeMessageFactory sMimeMessageFactory;

    private final Tracer tracer;

    private PeppolAs2SystemIdentifier as2SystemIdentifierOfSender;

    @Inject
    public As2MessageSender(KeystoreManager keystoreManager, As2TransmissionEvidenceFactory as2TransmissionEvidenceFactory, Tracer tracer) {
        this.keystoreManager = keystoreManager;
        this.as2TransmissionEvidenceFactory = as2TransmissionEvidenceFactory;
        this.sMimeMessageFactory = new SMimeMessageFactory(keystoreManager.getOurPrivateKey(), keystoreManager.getOurCertificate());
        this.tracer = tracer;

        // Establishes our AS2 System Identifier based upon the contents of the CN= field of the certificate
        as2SystemIdentifierOfSender = getAs2SystemIdentifierForSender();
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
        if (endpointAddress.getCommonName() == null) {
            throw new IllegalStateException("Must supply the X.509 common name (AS2 System Identifier) of the end point for AS2 protocol");
        }

        try (Span span = tracer.newChild(root.context()).name("Send AS2").start()) {
            SendResult sendResult = send(
                    transmissionRequest.getPayload(),
                    transmissionRequest.getHeader().getReceiver(),
                    transmissionRequest.getHeader().getSender(),
                    transmissionRequest.getMessageId(),
                    transmissionRequest.getHeader().getDocumentType(),
                    transmissionRequest.getEndpointAddress(),
                    span
            );

            return new As2TransmissionResponse(
                    sendResult.messageId,
                    transmissionRequest.getPeppolStandardBusinessHeader(),
                    endpointAddress.getUrl(),
                    endpointAddress.getTransportProfile(),
                    endpointAddress.getCommonName(),
                    sendResult.remEvidenceBytes,
                    sendResult.signedMimeMdnBytes
            );
        }
    }

    /**
     * This is the work horse method of this class, responsible for the actual http transmission.
     *
     * @throws OxalisTransmissionException
     */
    SendResult send(InputStream inputStream,
                    ParticipantIdentifier recipient,
                    ParticipantIdentifier sender,
                    MessageId messageId,
                    DocumentTypeIdentifier peppolDocumentTypeId,
                    PeppolEndpointData peppolEndpointData,
                    Span root) throws OxalisTransmissionException {

        final String endpointAddress;
        Mic mic;
        HttpPost httpPost;

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
                    signedMimeMessage = sMimeMessageFactory.createSignedMimeMessage(mimeBodyPart);
                } catch (MimeTypeParseException e) {
                    throw new IllegalStateException("Problems with MIME types: " + e.getMessage(), e);
                }


                endpointAddress = peppolEndpointData.getUrl().toExternalForm();
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
        try (Span span = tracer.newChild(root.context()).name("execute").start()) {
            try {
                span.tag("sender", sender.toString());
                span.tag("recipient", sender.toString());
                span.tag("endpointAddress", endpointAddress);
                span.tag("documentType", peppolDocumentTypeId.toString());

                CloseableHttpClient httpClient = createCloseableHttpClient();

                log.debug("Sending AS2 from {} to {} at {} type {}.", sender, recipient, endpointAddress, peppolDocumentTypeId);
                postResponse = httpClient.execute(httpPost);
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

                // Creates the REM evidence
                @NotNull As2RemWithMdnTransmissionEvidenceImpl evidence = getAs2RemWithMdnTransmissionEvidence(recipient, sender, messageId, peppolDocumentTypeId, signedMimeMDN);

                ByteArrayOutputStream evidenceBytes;
                try {
                    evidenceBytes = new ByteArrayOutputStream();
                    IOUtils.copy(evidence.getInputStream(), evidenceBytes);
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to transform transport evidence to byte array." + e.getMessage(), e);
                }

                return new SendResult(messageId, evidenceBytes.toByteArray(), MimeMessageHelper.toBytes(signedMimeMDN));
            } catch (RuntimeException e) {
                span.tag("exception", e.getMessage());
                throw e;
            }
        }
    }

    @NotNull
    private As2RemWithMdnTransmissionEvidenceImpl getAs2RemWithMdnTransmissionEvidence(ParticipantIdentifier recipient, ParticipantIdentifier sender, MessageId messageId, DocumentTypeIdentifier peppolDocumentTypeId, MimeMessage mimeMessage) {

        // Embeds the signed MDN into a generic a As2RemWithMdnTransmissionEvidenceImpl
        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
        Map<String, String> mdnFields = mdnMimeMessageInspector.getMdnFields();

        String messageDigestAsBase64 = mdnFields.get(MdnMimeMessageFactory.X_ORIGINAL_MESSAGE_DIGEST);
        if (messageDigestAsBase64 == null) {
            messageDigestAsBase64 = new String(Base64.getEncoder().encode("null".getBytes()));
        }
        String receptionTimeStampAsString = mdnFields.get(MdnMimeMessageFactory.X_PEPPOL_TIME_STAMP);
        Date receptionTimeStamp;
        if (receptionTimeStampAsString != null) {
            receptionTimeStamp = As2DateUtil.parseIso8601TimeStamp(receptionTimeStampAsString);
        } else {
            receptionTimeStamp = new Date();
        }

        As2RemWithMdnTransmissionEvidenceImpl evidence;
        TimeWatch evidenceCreatorWatch = TimeWatch.start("evidence");
        try (Span span = tracer.newTrace().name("Create REM evidence")) {
            evidence = as2TransmissionEvidenceFactory.createEvidence(EventCode.DELIVERY,
                    TransmissionRole.C_2,
                    mimeMessage,
                    recipient,
                    sender,
                    peppolDocumentTypeId,
                    receptionTimeStamp,
                    Base64.getDecoder().decode(messageDigestAsBase64),
                    messageId);
        }
        log.info("Creating REM evidence took " + evidenceCreatorWatch.time(TimeUnit.MILLISECONDS) + "ms");

        return evidence;
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
                if (peppolEndpointData.getCommonName() == null || !CommonName.valueOf(cert.getSubjectX500Principal()).equals(peppolEndpointData.getCommonName())) {
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
            return PeppolAs2SystemIdentifier.valueOf(CommonName.valueOf(ourCertificate.getSubjectX500Principal()));
        } catch (InvalidAs2SystemIdentifierException e) {
            throw new IllegalStateException("AS2 System Identifier could not be obtained from " + ourCertificate.getSubjectX500Principal(), e);
        }
    }


    CloseableHttpClient createCloseableHttpClient() {

        // "SSLv3" is disabled by default : http://www.apache.org/dist/httpcomponents/httpclient/RELEASE_NOTES-4.3.x.txt
        SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());

        return HttpClients.custom()
                .setRoutePlanner(routePlanner)
                .build();
    }
}
