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

package eu.peppol.as2;

import com.google.inject.Inject;
import eu.peppol.MessageDigestResult;
import eu.peppol.PeppolMessageMetaData;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.as2.evidence.As2TransmissionEvidenceFactory;
import eu.peppol.as2.servlet.ResponseData;
import eu.peppol.document.PayloadDigestCalculator;
import eu.peppol.document.Sbdh2PeppolHeaderConverter;
import eu.peppol.document.SbdhFastParser;
import eu.peppol.evidence.TransmissionEvidence;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.OxalisMessagePersistenceException;
import eu.peppol.security.OxalisCertificateValidator;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.util.OxalisConstant;
import eu.peppol.xsd.ticc.receipt._1.TransmissionRole;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * Main entry point for receiving AS2 messages.
 *
 * @author steinar
 * @author thore
 */
public class InboundMessageReceiver {

    public static final Logger log = LoggerFactory.getLogger(InboundMessageReceiver.class);

    private final As2MessageInspector as2MessageInspector;
    private final MdnMimeMessageFactory mdnMimeMessageFactory;
    private final SbdhFastParser sbdhFastParser;
    private final MessageRepository messageRepository;
    private final RawStatisticsRepository rawStatisticsRepository;
    private final AccessPointIdentifier ourAccessPointIdentifier;
    private final OxalisCertificateValidator oxalisCertificateValidator;
    private final As2TransmissionEvidenceFactory as2TransmissionEvidenceFactory;

    @Inject
    public InboundMessageReceiver(MdnMimeMessageFactory mdnMimeMessageFactory,
                                  SbdhFastParser sbdhFastParser,
                                  As2MessageInspector as2MessageInspector,
                                  MessageRepository messageRepository,
                                  RawStatisticsRepository rawStatisticsRepository,
                                  AccessPointIdentifier ourAccessPointIdentifier,
                                  OxalisCertificateValidator oxalisCertificateValidator,
                                  As2TransmissionEvidenceFactory as2TransmissionEvidenceFactory) {
        this.mdnMimeMessageFactory = mdnMimeMessageFactory;
        this.sbdhFastParser = sbdhFastParser;
        this.as2MessageInspector = as2MessageInspector;
        this.messageRepository = messageRepository;
        this.rawStatisticsRepository = rawStatisticsRepository;
        this.ourAccessPointIdentifier = ourAccessPointIdentifier;
        this.oxalisCertificateValidator = oxalisCertificateValidator;
        this.as2TransmissionEvidenceFactory = as2TransmissionEvidenceFactory;

        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());

        // Sanity checks
        if (messageRepository == null) {
            throw new IllegalArgumentException("messageRepository is a required argument in constructor");
        }
        if (mdnMimeMessageFactory == null) {
            throw new IllegalArgumentException("MdnMimeMessageFactory is required argument");
        }

    }

    /**
     * Receives an AS2 Message in the form of a map of headers together with the payload,
     * which is made available in an input stream
     * <p>
     * If persisting message to MessageRepository fails, we have to return negative MDN.
     *
     * @param httpHeaders the http headers received
     * @param inputStream supplies the actual data stream
     * @return MDN object to signal if everything is ok or if some error occurred while receiving
     * @throws ErrorWithMdnException if validation fails due to syntactic, semantic or other reasons.
     */
    public ResponseData receive(InternetHeaders httpHeaders, InputStream inputStream) {
        if (httpHeaders == null) {
            throw new IllegalArgumentException("httpHeaders required constructor argument");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream required constructor argument");
        }

        Mic mic = null;
        try {

            log.debug("Receiving message ..");

            // Inspects the eu.peppol.as2.As2Header.DISPOSITION_NOTIFICATION_OPTIONS

            // TODO: Move this tp As2MessageFactory.createAs2MessageFrom
            inspectDispositionNotificationOptions(httpHeaders);

            log.debug("Message contains valid AS2 Disposition-notification-options, now creating internal AS2 message...");

            MimeMessage mimeMessage = MimeMessageHelper.createMimeMessageAssistedByHeaders(inputStream, httpHeaders);
            SignedMimeMessage signedMimeMessage = new SignedMimeMessage(mimeMessage);

            // Transforms the input data into a proper As2Message
            As2Message as2Message = As2MessageFactory.createAs2MessageFrom(httpHeaders, signedMimeMessage);


            // Validates the message headers according to the PEPPOL rules and performs semantic validation
            log.debug("Validating AS2 Message: " + as2Message);
            as2MessageInspector.validate(as2Message);

            // Extracts the SBDH from the message, the SBDH is required by OpenPEPPOL
            StandardBusinessDocumentHeader sbdh = sbdhFastParser.parse(as2Message.getSignedMimeMessage().getPayload());
            if (sbdh == null) {
                throw new IllegalStateException("Payload does not contain Standard Business Document Header (SBDH)");
            }

            // Calculates the message digest of the payload, there are two alternatives:
            // a) if the payload consists of SBDH + XML document -> calculate digest over entire payload
            // b) if payload consists of SBDH + ASiC -> calculate digest of binary ASiC archive only. I.e. without the SBDH and
            //    base64 decoded.
            MessageDigestResult payloadDigestResult = PayloadDigestCalculator.calcDigest(OxalisConstant.DEFAULT_DIGEST_ALGORITHM, sbdh, as2Message.getSignedMimeMessage().getPayload());
            log.debug("The MessageDigest of the payload is " + new String(Base64.encode(payloadDigestResult.getDigest())));

            // Persists the payload
            PeppolMessageMetaData peppolMessageMetaData = persistPayload(sbdh, messageRepository, as2Message);

            // Creates the MDN data to be returned (not the actual MDN, which must be represented as an S/MIME message)
            // Calculates the MIC for the payload using the preferred mic algorithm
            String micAlgorithmName = as2Message.getDispositionNotificationOptions().getPreferredSignedReceiptMicAlgorithmName();
            mic = as2Message.getSignedMimeMessage().calculateMic(micAlgorithmName);
            log.debug("Calculated MIC : " + mic.toString());
            MdnData mdnData = createMdnData(httpHeaders, mic, payloadDigestResult);

            // Finally we persist the raw statistics data
            persistStatistics(rawStatisticsRepository, ourAccessPointIdentifier, peppolMessageMetaData);

            // Creates the S/MIME message to be returned to the sender
            MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(mdnData, httpHeaders);
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                signedMdn.writeTo(byteArrayOutputStream);
                messageRepository.saveNativeTransportReceipt(byteArrayOutputStream.toByteArray());
            } catch (IOException | MessagingException e) {
                log.error("Unable to write signed mdn to byte array:" + e.getMessage(),e);
            }

            // Creates the REM evidence and persists it
            TransmissionEvidence remWithMdnEvidence = as2TransmissionEvidenceFactory.createRemWithMdnEvidence(mdnData, peppolMessageMetaData, signedMdn, TransmissionRole.C_3);
            messageRepository.saveTransportReceipt(remWithMdnEvidence,peppolMessageMetaData);

            // Returns the response to be emitted by whoever is calling us
            ResponseData responseData = new ResponseData(HttpServletResponse.SC_OK, signedMdn, mdnData);
            return responseData;

        } catch (InvalidAs2MessageException | MdnRequestException | OxalisMessagePersistenceException e) {
            log.error("Invalid AS2 message: " + e.getMessage(), e);

            MdnData mdnData = MdnData.Builder.buildFailureFromHeaders(httpHeaders, mic, e.getMessage());
            MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(mdnData, httpHeaders);

            ResponseData responseDataWithErrors = new ResponseData(HttpServletResponse.SC_BAD_REQUEST, signedMdn, mdnData);
            return responseDataWithErrors;
        }
    }

    protected PeppolMessageMetaData persistPayload(StandardBusinessDocumentHeader sbdh, MessageRepository messageRepository, As2Message as2Message) throws OxalisMessagePersistenceException {

        log.debug("Persisting AS2 Message ....");

        PeppolMessageMetaData peppolMessageMetaData = collectTransmissionMetaData(as2Message, sbdh);

        // Performs the actual persistence by invoking whatever has been configured for persistence
        messageRepository.saveInboundMessage(peppolMessageMetaData, as2Message.getSignedMimeMessage().getPayload());

        return peppolMessageMetaData;
    }

    /**
     * Creates a message digest using our preferred algorithm
     *
     * @return
     */
    MessageDigest createMessageDigest() {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(OxalisConstant.DEFAULT_DIGEST_ALGORITHM, new BouncyCastleProvider());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to create MessageDigest object for algortihm : ", e);
        }
        return messageDigest;
    }

    protected MdnData createMdnData(InternetHeaders internetHeaders, Mic mic, MessageDigestResult messageDigestResult) {
        MdnData mdnData = MdnData.Builder.buildProcessedOK(internetHeaders, mic, messageDigestResult);
        log.debug("Message received OK, MDN returned will be: " + mdnData);
        return mdnData;
    }

    protected void persistStatistics(RawStatisticsRepository rawStatisticsRepository, AccessPointIdentifier ourAccessPointIdentifier, PeppolMessageMetaData peppolMessageMetaData) {
        // Persists raw statistics when message was received (ignore if stats couldn't be persisted, just warn)
        try {
            RawStatistics rawStatistics = new RawStatistics.RawStatisticsBuilder()
                    .accessPointIdentifier(ourAccessPointIdentifier)
                    .inbound()
                    .documentType(peppolMessageMetaData.getDocumentTypeIdentifier())
                    .sender(peppolMessageMetaData.getSenderId())
                    .receiver(peppolMessageMetaData.getRecipientId())
                    .profile(peppolMessageMetaData.getProfileTypeIdentifier())
                    .channel(new ChannelId("AS2"))
                    .build();
            rawStatisticsRepository.persist(rawStatistics);
        } catch (Exception e) {
            log.error("Unable to persist statistics for " + peppolMessageMetaData.toString() + "; " + e.getMessage(), e);
            log.error("Message has been persisted and confirmation sent, but you must investigate this error");
        }
    }

    /**
     * Extracts data from the SBDH received, which we need for handling the message received.
     *
     * @param as2Message
     * @param sbdh
     * @return
     */
    PeppolMessageMetaData collectTransmissionMetaData(As2Message as2Message, StandardBusinessDocumentHeader sbdh) {

        // Converts the SBDH into a PEPPOL header
        PeppolStandardBusinessHeader peppolStandardBusinessHeader = Sbdh2PeppolHeaderConverter.convertSbdh2PeppolHeader(sbdh);

        PeppolMessageMetaData peppolMessageMetaData = new PeppolMessageMetaData();
        peppolMessageMetaData.setTransmissionId(as2Message.getTransmissionId());
        peppolMessageMetaData.setMessageId(peppolStandardBusinessHeader.getMessageId().toString());
        peppolMessageMetaData.setSenderId(peppolStandardBusinessHeader.getSenderId());
        peppolMessageMetaData.setRecipientId(peppolStandardBusinessHeader.getRecipientId());
        peppolMessageMetaData.setDocumentTypeIdentifier(peppolStandardBusinessHeader.getDocumentTypeIdentifier());
        peppolMessageMetaData.setProfileTypeIdentifier(peppolStandardBusinessHeader.getProfileTypeIdentifier());
        peppolMessageMetaData.setSendingAccessPointId(new AccessPointIdentifier(as2Message.getAs2From().toString()));
        peppolMessageMetaData.setReceivingAccessPoint(new AccessPointIdentifier(as2Message.getAs2To().toString()));

        // Retrieves the Common Name of the X500Principal, which is used to construct the AccessPointIdentifier for the senders access point
        X500Principal subjectX500Principal = as2Message.getSignedMimeMessage().getSignersX509Certificate().getSubjectX500Principal();
        peppolMessageMetaData.setSendingAccessPointPrincipal(subjectX500Principal);

        return peppolMessageMetaData;

    }

    private void inspectDispositionNotificationOptions(InternetHeaders internetHeaders) throws MdnRequestException {
        String[] headerValue = internetHeaders.getHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName());
        if (headerValue == null || headerValue[0] == null) {
            throw new MdnRequestException("AS2 header '" + As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName() + "' not found in request");
        }
        // Attempts to parseMultipart the Disposition Notification Options
        String value = headerValue[0];
        As2DispositionNotificationOptions as2DispositionNotificationOptions = As2DispositionNotificationOptions.valueOf(value);
        String micAlgorithm = as2DispositionNotificationOptions.getPreferredSignedReceiptMicAlgorithmName();
        if (!"sha1".equalsIgnoreCase(micAlgorithm)) {
            throw new MdnRequestException("Invalid MIC algorithm, only SHA1 supported:" + micAlgorithm);
        }
    }

}