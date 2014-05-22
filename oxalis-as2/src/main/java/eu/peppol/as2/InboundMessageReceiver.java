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
import eu.peppol.document.SbdhParser;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.identifier.AccessPointIdentifier;
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
 *         Date: 20.10.13
 *         Time: 10:45
 */
public class InboundMessageReceiver {

    public static final Logger log = LoggerFactory.getLogger(InboundMessageReceiver.class);
    private final SbdhParser sbdhParser;

    public InboundMessageReceiver() {
        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());
        sbdhParser = new SbdhParser();
    }

    /**
     * Receives an AS2 Message in the form of a map of headers together with the payload, which is made available
     * in an input stream
     *
     *
     *
     *
     *
     * @param internetHeaders
     * @param inputStream  supplies the actual data
     * @param messageRepository
     * @return MDN object if everything is ok.
     * @throws ErrorWithMdnException if validation fails due to syntactic, semantic or other reasons.
     */
    public MdnData receive(InternetHeaders internetHeaders, InputStream inputStream, MessageRepository messageRepository) throws ErrorWithMdnException {

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

            log.info("Receiving message ..");
            // Inspects the eu.peppol.as2.As2Header.DISPOSITION_NOTIFICATION_OPTIONS
            inspectDispositionNotificationOptions(internetHeaders);

            log.info("Message contains valid Disposition-notification-options, now creating internal AS2 message...");
            // Transforms the input data into a proper As2Message
            As2Message as2Message = As2MessageFactory.createAs2MessageFrom(internetHeaders, inputStream);

            log.info("Validating AS2 Message: " + as2Message);
            // Validates the message headers according to the PEPPOL rules and performs semantic validation
            SignedMimeMessageInspector SignedMimeMessageInspector = As2MessageInspector.validate(as2Message);

            // Calculates the MIC for the payload using the preferred mic algorithm
            String micAlgorithmName = as2Message.getDispositionNotificationOptions().getPreferredSignedReceiptMicAlgorithmName();
            mic = SignedMimeMessageInspector.calculateMic(micAlgorithmName);
            log.info("Calculated MIC : " + mic.toString());

            // TODO use the DispositionModifier to throw the right MDN exception
            // As2Disposition.DispositionModifier.unsupportedFormatFailure()

            // Persists the payload
            log.info("Persisting AS2 Message ....");
            InputStream payloadInputStream = SignedMimeMessageInspector.getPayload();
            PeppolMessageMetaData peppolMessageMetaData = collectTransmissionData(as2Message, SignedMimeMessageInspector);
            messageRepository.saveInboundMessage(peppolMessageMetaData, payloadInputStream);

            log.info("Persisting RAW statistics was NOT saved for this message ....");
            // smimeMessageInspector.getMimeMessage().writeTo(System.out);
            // TODO we optionally call rawStatisticsRepository.persist() from here

            // Creates the MDN to be returned
            MdnData mdnData = MdnData.Builder.buildProcessedOK(internetHeaders, mic);
            log.info("Message received OK, MDN returned: " + mdnData);
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

        DocumentSniffer documentSniffer = new DocumentSniffer(SignedMimeMessageInspector.getPayload());
        if (!documentSniffer.isSbdhDetected()) {
            throw new IllegalStateException("Payload does not contain Standard Business Document Header");
        }

        // Parses the SBDH and obtains metadata
        PeppolStandardBusinessHeader peppolStandardBusinessHeader = sbdhParser.parse(SignedMimeMessageInspector.getPayload());

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