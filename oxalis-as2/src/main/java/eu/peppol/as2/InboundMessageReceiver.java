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

import eu.peppol.PeppolMessageMetaData;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.DocumentSniffer;
import eu.peppol.document.DocumentSnifferSimpleImpl;
import eu.peppol.document.Sbdh2PeppolHeaderParser;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.InternetHeaders;
import javax.security.auth.x500.X500Principal;
import java.io.InputStream;
import java.security.Security;

/**
 * Main entry point for receiving AS2 messages.
 *
 * @author steinar
 * @author thore
 */
public class InboundMessageReceiver {

    public static final Logger log = LoggerFactory.getLogger(InboundMessageReceiver.class);

    private final Sbdh2PeppolHeaderParser sbdh2PeppolHeaderParser;

    public InboundMessageReceiver() {
        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());
        sbdh2PeppolHeaderParser = new Sbdh2PeppolHeaderParser();
    }

    /**
     * Receives an AS2 Message in the form of a map of headers together with the payload,
     * which is made available in an input stream
     *
     * If persisting message to MessageRepository fails, we have to return negative MDN.
     *
     * @param internetHeaders the http headers received
     * @param inputStream supplies the actual data stream
     * @param messageRepository the repository to which we store inbound messages
     * @param rawStatisticsRepository the repository to which we store raw statistics when reception successful
     * @param ourAccessPointIdentifier out accesspoint identifer (CN of the certificate used)
     * @return MDN object to signal if everything is ok or if some error occurred while receiving
     * @throws ErrorWithMdnException if validation fails due to syntactic, semantic or other reasons.
     */
    public MdnData receive(
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

            log.debug("Message contains valid Disposition-notification-options, now creating internal AS2 message...");
            // Transforms the input data into a proper As2Message
            As2Message as2Message = As2MessageFactory.createAs2MessageFrom(internetHeaders, inputStream);

            log.debug("Validating AS2 Message: " + as2Message);
            // Validates the message headers according to the PEPPOL rules and performs semantic validation
            SignedMimeMessageInspector SignedMimeMessageInspector = As2MessageInspector.validate(as2Message);

            // Calculates the MIC for the payload using the preferred mic algorithm
            String micAlgorithmName = as2Message.getDispositionNotificationOptions().getPreferredSignedReceiptMicAlgorithmName();
            mic = SignedMimeMessageInspector.calculateMic(micAlgorithmName);
            log.debug("Calculated MIC : " + mic.toString());

            // TODO use the DispositionModifier to throw the right MDN exception
            // As2Disposition.DispositionModifier.unsupportedFormatFailure()

            // Persists the payload
            log.debug("Persisting AS2 Message ....");
            InputStream payloadInputStream = SignedMimeMessageInspector.getPayload();
            PeppolMessageMetaData peppolMessageMetaData = collectTransmissionData(as2Message, SignedMimeMessageInspector);
            messageRepository.saveInboundMessage(peppolMessageMetaData, payloadInputStream);

            // Creates the MDN to be returned
            MdnData mdnData = MdnData.Builder.buildProcessedOK(internetHeaders, mic);
            log.debug("Message received OK, MDN returned: " + mdnData);

            // smimeMessageInspector.getMimeMessage().writeTo(System.out);

            // Persist raw statistics when message was received (ignore if stats couldn't be persisted, just warn)
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

            return mdnData;

        } catch (InvalidAs2MessageException e) {
            log.error("Invalid AS2 message " + e.getMessage(), e);
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(internetHeaders, mic, e.getMessage());
            throw new ErrorWithMdnException(mdnData);

        } catch (MdnRequestException e) {
            log.error("Invalid MDN request: " + e.getMessage());
            MdnData mdnData = MdnData.Builder.buildFailureFromHeaders(internetHeaders, mic, e.getMessage());
            throw new ErrorWithMdnException(mdnData);

        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage(), e);
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(internetHeaders, mic, e.getMessage());
            throw new ErrorWithMdnException(mdnData, e);
        }

    }

    PeppolMessageMetaData collectTransmissionData(As2Message as2Message, SignedMimeMessageInspector SignedMimeMessageInspector) {

        DocumentSniffer documentSniffer = new DocumentSnifferSimpleImpl(SignedMimeMessageInspector.getPayload());
        if (!documentSniffer.isSbdhDetected()) {
            throw new IllegalStateException("Payload does not contain Standard Business Document Header");
        }

        // Parses the SBDH and obtains metadata
        PeppolStandardBusinessHeader peppolStandardBusinessHeader = sbdh2PeppolHeaderParser.parse(SignedMimeMessageInspector.getPayload());

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

}