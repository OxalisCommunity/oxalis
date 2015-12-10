/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.as2;

import com.google.inject.Inject;
import eu.peppol.MessageDigestResult;
import eu.peppol.PeppolMessageMetaData;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.Sbdh2PeppolHeaderConverter;
import eu.peppol.document.SbdhFastParser;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.OxalisMessagePersistenceException;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import javax.mail.internet.InternetHeaders;
import javax.security.auth.x500.X500Principal;
import java.io.InputStream;
import java.security.DigestInputStream;
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
    public static final String DIGEST_ALGORITHM = "SHA-256";

    private final SbdhFastParser sbdhFastParser;
    private final As2MessageInspector as2MessageInspector;

    @Inject
    public InboundMessageReceiver(SbdhFastParser sbdhFastParser, As2MessageInspector as2MessageInspector) {
        this.sbdhFastParser = sbdhFastParser;
        this.as2MessageInspector = as2MessageInspector;
        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Receives an AS2 Message in the form of a map of headers together with the payload,
     * which is made available in an input stream
     * <p>
     * If persisting message to MessageRepository fails, we have to return negative MDN.
     *
     * @param internetHeaders          the http headers received
     * @param inputStream              supplies the actual data stream
     * @param messageRepository        the repository to which we store inbound messages
     * @param rawStatisticsRepository  the repository to which we store raw statistics when reception successful
     * @param ourAccessPointIdentifier out accesspoint identifer (CN of the certificate used)
     * @return MDN object to signal if everything is ok or if some error occurred while receiving
     * @throws ErrorWithMdnException if validation fails due to syntactic, semantic or other reasons.
     */
    public As2ReceiptData receive(
            InternetHeaders internetHeaders,
            InputStream inputStream,
            MessageRepository messageRepository,
            RawStatisticsRepository rawStatisticsRepository,
            AccessPointIdentifier ourAccessPointIdentifier
    ) throws ErrorWithMdnException {

        if (messageRepository == null) {
            throw new IllegalArgumentException("messageRepository is a required argument in constructor");
        }
        if (internetHeaders == null) {
            throw new IllegalArgumentException("internetHeaders required constructor argument");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream required constructor argument");
        }

        Mic mic = null;
        try {

            log.debug("Receiving message ..");
            // Inspects the eu.peppol.as2.As2Header.DISPOSITION_NOTIFICATION_OPTIONS
            inspectDispositionNotificationOptions(internetHeaders);

            log.debug("Message contains valid AS2 Disposition-notification-options, now creating internal AS2 message...");
            // Transforms the input data into a proper As2Message
            As2Message as2Message = As2MessageFactory.createAs2MessageFrom(internetHeaders, inputStream);

            log.debug("Validating AS2 Message: " + as2Message);
            // Validates the message headers according to the PEPPOL rules and performs semantic validation
            SignedMimeMessageInspector signedMimeMessageInspector = as2MessageInspector.validate(as2Message);

            // Calculates the MIC for the payload using the preferred mic algorithm
            String micAlgorithmName = as2Message.getDispositionNotificationOptions().getPreferredSignedReceiptMicAlgorithmName();
            mic = signedMimeMessageInspector.calculateMic(micAlgorithmName);
            log.debug("Calculated MIC : " + mic.toString());

            // Persists the payload
            PersistenceAndDigestResult persistenceAndDigestResult = persistPayloadAndComputeDigest(messageRepository, as2Message, signedMimeMessageInspector);

            // Creates the MDN data to be returned (not the actual MDN)
            MdnData mdnData = createMdnData(internetHeaders, mic, persistenceAndDigestResult.getMessageDigestResult());

            // Finally we persist the statistics data
            persistStatistics(rawStatisticsRepository, ourAccessPointIdentifier, persistenceAndDigestResult.getPeppolMessageMetaData());

            return new As2ReceiptData(mdnData, persistenceAndDigestResult.getPeppolMessageMetaData());

        } catch (InvalidAs2MessageException e) {
            log.error("Invalid AS2 message " + e.getMessage(), e);
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(internetHeaders, mic, e.getMessage());
            throw new ErrorWithMdnException(mdnData,e);

        } catch (MdnRequestException e) {
            log.error("Invalid MDN request: " + e.getMessage());
            MdnData mdnData = MdnData.Builder.buildFailureFromHeaders(internetHeaders, mic, e.getMessage());
            throw new ErrorWithMdnException(mdnData,e);

        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage(), e);
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(internetHeaders, mic, e.getMessage());
            throw new ErrorWithMdnException(mdnData, e);
        }
    }

    protected PersistenceAndDigestResult persistPayloadAndComputeDigest(MessageRepository messageRepository, As2Message as2Message, SignedMimeMessageInspector signedMimeMessageInspector) throws OxalisMessagePersistenceException {

        log.debug("Persisting AS2 Message ....");
        PeppolMessageMetaData peppolMessageMetaData = collectTransmissionMetaData(as2Message, signedMimeMessageInspector);

        // We calculate the digest while we read the data from the Mime message and shove it into persistent storage
        // thus killing two birds with one stone.
        MessageDigest messageDigest = createMessageDigest();

        DigestInputStream digestInputStream = new DigestInputStream(signedMimeMessageInspector.getPayload(), messageDigest);

        // Performs the actual persistence by invoking whatever has been configured for persistence
        messageRepository.saveInboundMessage(peppolMessageMetaData, digestInputStream);

        // Saves the calculated message digest
        MessageDigestResult messageDigestResult = new MessageDigestResult(messageDigest.digest(), messageDigest.getAlgorithm());


        return new PersistenceAndDigestResult(peppolMessageMetaData, messageDigestResult);
    }

    /**
     * Creates a message digest using our preferred algorithm
     *
     * @return
     */
    MessageDigest createMessageDigest() {
        MessageDigest messageDigest;
        try {
             messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM, new BouncyCastleProvider());
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
     * @param SignedMimeMessageInspector
     * @return
     */
    PeppolMessageMetaData collectTransmissionMetaData(As2Message as2Message, SignedMimeMessageInspector SignedMimeMessageInspector) {


        StandardBusinessDocumentHeader sbdh = sbdhFastParser.parse(SignedMimeMessageInspector.getPayload());
        if (sbdh == null) {
            throw new IllegalStateException("Payload does not contain Standard Business Document Header");
        }

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
        X500Principal subjectX500Principal = SignedMimeMessageInspector.getSignersX509Certificate().getSubjectX500Principal();
        peppolMessageMetaData.setSendingAccessPointPrincipal(subjectX500Principal);

        return peppolMessageMetaData;

    }

    private As2DispositionNotificationOptions inspectDispositionNotificationOptions(InternetHeaders internetHeaders) throws MdnRequestException {
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
        return as2DispositionNotificationOptions;
    }

    static class PersistenceAndDigestResult {
        PeppolMessageMetaData peppolMessageMetaData;
        MessageDigestResult messageDigestResult;

        public PersistenceAndDigestResult(PeppolMessageMetaData peppolMessageMetaData, MessageDigestResult messageDigestResult) {
            this.peppolMessageMetaData = peppolMessageMetaData;
            this.messageDigestResult = messageDigestResult;
        }

        public PeppolMessageMetaData getPeppolMessageMetaData() {
            return peppolMessageMetaData;
        }

        public MessageDigestResult getMessageDigestResult() {
            return messageDigestResult;
        }
    }

}