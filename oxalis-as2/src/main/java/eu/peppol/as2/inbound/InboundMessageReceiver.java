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

package eu.peppol.as2.inbound;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.sun.mail.util.LineOutputStream;
import eu.peppol.MessageDigestResult;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.PeppolTransmissionMetaData;
import eu.peppol.as2.lang.InvalidAs2MessageException;
import eu.peppol.as2.lang.MdnRequestException;
import eu.peppol.as2.model.*;
import eu.peppol.as2.util.*;
import eu.peppol.document.PayloadDigestCalculator;
import eu.peppol.document.SbdhFastParser;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.OxalisMessagePersistenceException;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.util.OxalisConstant;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.commons.bouncycastle.BCHelper;
import no.difi.oxalis.commons.io.PeekingInputStream;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.sbdh.SbdReader;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

/**
 * Main entry point for receiving AS2 messages.
 * <p>
 * TODO: Certificate validation
 *
 * @author steinar
 * @author thore
 */
class InboundMessageReceiver {

    public static final Logger log = LoggerFactory.getLogger(InboundMessageReceiver.class);

    private final As2MessageInspector as2MessageInspector;

    private final MdnMimeMessageFactory mdnMimeMessageFactory;

    private final MessageRepository messageRepository;

    private final RawStatisticsRepository rawStatisticsRepository;

    private final AccessPointIdentifier ourAccessPointIdentifier;

    private final TimestampProvider timestampProvider;

    static {
        BCHelper.registerProvider();
    }

    @Inject
    public InboundMessageReceiver(MdnMimeMessageFactory mdnMimeMessageFactory,
                                  As2MessageInspector as2MessageInspector,
                                  MessageRepository messageRepository,
                                  RawStatisticsRepository rawStatisticsRepository,
                                  AccessPointIdentifier ourAccessPointIdentifier,
                                  TimestampProvider timestampProvider) {
        this.mdnMimeMessageFactory = mdnMimeMessageFactory;
        this.as2MessageInspector = as2MessageInspector;
        this.messageRepository = messageRepository;
        this.rawStatisticsRepository = rawStatisticsRepository;
        this.ourAccessPointIdentifier = ourAccessPointIdentifier;
        this.timestampProvider = timestampProvider;
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

            // Inspects the eu.peppol.as2.model.As2Header.DISPOSITION_NOTIFICATION_OPTIONS

            // TODO: Move this tp As2MessageFactory.createAs2MessageFrom
            inspectDispositionNotificationOptions(httpHeaders);

            log.debug("Message contains valid AS2 Disposition-notification-options, now creating internal AS2 message...");

            long start = System.nanoTime();
            MimeMessage mimeMessage = MimeMessageHelper.createMimeMessageAssistedByHeaders(inputStream, httpHeaders);

            try {
                MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();

                // Extract signature
                byte[] signature = ByteStreams.toByteArray(((InputStream) mimeMultipart.getBodyPart(1).getContent()));

                // Get timestamp
                Timestamp t2 = timestampProvider.generate(signature);

                // TODO Validate signature

                // TODO Validate certificate

                // Extract signed digest and digest algorithm
                SMimeDigestMethod digestMethod = inspectDispositionNotificationOptions(httpHeaders);

                // Extract body content
                MimeBodyPart mimeBodyPart = (MimeBodyPart) mimeMultipart.getBodyPart(0);

                // Extract content headers
                byte[] headerBytes = extractHeader(mimeBodyPart);
                // System.out.println(new String(headerBytes));

                // Prepare calculation of digest
                MessageDigest messageDigest = MessageDigest.getInstance(digestMethod.getMethod(), BouncyCastleProvider.PROVIDER_NAME);
                InputStream digestInputStream = new DigestInputStream(mimeBodyPart.getInputStream(), messageDigest);

                // Add header to calculation of digest
                messageDigest.update(headerBytes);

                // Prepare content for reading of SBDH
                PeekingInputStream peekingInputStream = new PeekingInputStream(digestInputStream);

                // Extract SBDH
                Header header;
                try (SbdReader sbdReader = SbdReader.newInstance(peekingInputStream)) {
                    header = sbdReader.getHeader();
                }
                // log.info("Header: {}", header);

                // TODO Perform validation of SBDH

                // TODO Persist content
                ByteStreams.exhaust(peekingInputStream.newInputStream());

                log.info("Digest: {}", java.util.Base64.getEncoder().encodeToString(messageDigest.digest()));

                // TODO Compare calculated and signed digest

                // TODO Persist receipt

                // TODO Persist statistics
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }

            log.debug("Converted InputStream to MIME message in " + TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS));

            SignedMimeMessage signedMimeMessage = new SignedMimeMessage(mimeMessage);
            log.debug("MIME message converted to S/MIME message");

            // Transforms the input data into a proper As2Message
            As2Message as2Message = As2MessageFactory.createAs2MessageFrom(httpHeaders, signedMimeMessage);


            // Validates the message headers according to the PEPPOL rules and performs semantic validation
            log.debug("Validating AS2 Message: " + as2Message);
            as2MessageInspector.validate(as2Message);

            // Extracts the SBDH from the message, the SBDH is required by OpenPEPPOL.
            // Throws IllegalStateException if anything goes wrong.
            PeppolStandardBusinessHeader sbdh = SbdhFastParser.parse(as2Message.getSignedMimeMessage().getPayload());

            // Calculates the message digest of the payload, there are two alternatives:
            // a) if the payload consists of SBDH + XML document -> calculate digest over entire payload
            // b) if payload consists of SBDH + ASiC -> calculate digest of binary ASiC archive only. I.e. without the SBDH and
            //    base64 decoded.
            MessageDigestResult payloadDigestResult = PayloadDigestCalculator.calcDigest(OxalisConstant.DEFAULT_DIGEST_ALGORITHM, as2Message.getSignedMimeMessage().getPayload());
            log.debug("The MessageDigest of the payload is " + new String(Base64.encode(payloadDigestResult.getDigest())));

            // Persists the payload
            PeppolTransmissionMetaData peppolTransmissionMetaData = persistPayload(sbdh, messageRepository, as2Message);

            // Creates the MDN data to be returned (not the actual MDN, which must be represented as an S/MIME message)
            // Calculates the MIC for the payload using the preferred mic algorithm
            String micAlgorithmName = as2Message.getDispositionNotificationOptions().getPreferredSignedReceiptMicAlgorithmName();
            mic = as2Message.getSignedMimeMessage().calculateMic(micAlgorithmName);
            log.info("Calculated MIC : " + mic.toString());
            MdnData mdnData = createMdnData(httpHeaders, mic, payloadDigestResult);

            // Finally we persist the raw statistics data
            persistStatistics(rawStatisticsRepository, ourAccessPointIdentifier, peppolTransmissionMetaData);

            // Grabs the S/MIME message to be returned to the sender
            MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(mdnData, httpHeaders);

            // Returns the response to be emitted by whoever is calling us
            return new ResponseData(HttpServletResponse.SC_OK, signedMdn, mdnData);
        } catch (InvalidAs2MessageException | MdnRequestException | OxalisMessagePersistenceException e) {
            log.error("Invalid AS2 message: " + e.getMessage(), e);

            MdnData mdnData = MdnData.Builder.buildFailureFromHeaders(httpHeaders, mic, e.getMessage());
            MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(mdnData, httpHeaders);

            return new ResponseData(HttpServletResponse.SC_BAD_REQUEST, signedMdn, mdnData);
        }
    }

    protected byte[] extractHeader(MimeBodyPart mimeBody)
            throws MessagingException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LineOutputStream los = new LineOutputStream(outputStream);

        Enumeration hdrLines = mimeBody.getNonMatchingHeaderLines(new String[]{});
        while (hdrLines.hasMoreElements())
            los.writeln((String) hdrLines.nextElement());

        // The CRLF separator between header and content
        los.writeln();
        los.close();

        return outputStream.toByteArray();
    }

    protected PeppolTransmissionMetaData persistPayload(PeppolStandardBusinessHeader sbdh, MessageRepository messageRepository, As2Message as2Message) throws OxalisMessagePersistenceException {

        log.debug("Persisting AS2 Message ....");

        long start = System.nanoTime();
        PeppolTransmissionMetaData peppolTransmissionMetaData = collectTransmissionMetaData(as2Message, sbdh);

        // Performs the actual persistence by invoking whatever has been configured for persistence
        messageRepository.saveInboundMessage(peppolTransmissionMetaData, as2Message.getSignedMimeMessage().getPayload());

        long elapsed = System.nanoTime() - start;
        log.debug("Persistence of payload took " + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS) + "ms");

        return peppolTransmissionMetaData;
    }

    protected MdnData createMdnData(InternetHeaders internetHeaders, Mic mic, MessageDigestResult messageDigestResult) {
        MdnData mdnData = MdnData.Builder.buildProcessedOK(internetHeaders, mic, messageDigestResult);
        log.debug("Message received OK, MDN returned will be: " + mdnData);
        return mdnData;
    }

    protected void persistStatistics(RawStatisticsRepository rawStatisticsRepository, AccessPointIdentifier ourAccessPointIdentifier, PeppolTransmissionMetaData peppolTransmissionMetaData) {
        // Persists raw statistics when message was received (ignore if stats couldn't be persisted, just warn)
        long start = System.nanoTime();
        try {
            RawStatistics rawStatistics = new RawStatistics.RawStatisticsBuilder()
                    .accessPointIdentifier(ourAccessPointIdentifier)
                    .inbound()
                    .documentType(peppolTransmissionMetaData.getDocumentTypeIdentifier())
                    .sender(peppolTransmissionMetaData.getSenderId())
                    .receiver(peppolTransmissionMetaData.getRecipientId())
                    .profile(peppolTransmissionMetaData.getProfileTypeIdentifier())
                    .channel(new ChannelId("AS2"))
                    .build();
            rawStatisticsRepository.persist(rawStatistics);
            long elapsed = System.nanoTime() - start;
            log.debug("Persisting raw statistics took " + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS) + "ms");
        } catch (Exception e) {
            log.error("Unable to persist statistics for " + peppolTransmissionMetaData.toString() + ";\n " + e.getMessage(), e);
            log.error("Message has been persisted and confirmation sent, but you must investigate this error");
        }
    }

    /**
     * Extracts data from the SBDH received, which we need for handling the message received.
     */
    protected PeppolTransmissionMetaData collectTransmissionMetaData(As2Message as2Message, PeppolStandardBusinessHeader sbdh) {

        PeppolTransmissionMetaData peppolTransmissionMetaData = new PeppolTransmissionMetaData();
        peppolTransmissionMetaData.setMessageId(as2Message.getMessageId());
        peppolTransmissionMetaData.setSenderId(sbdh.getSenderId());
        peppolTransmissionMetaData.setRecipientId(sbdh.getRecipientId());
        peppolTransmissionMetaData.setDocumentTypeIdentifier(sbdh.getDocumentTypeIdentifier());
        peppolTransmissionMetaData.setProfileTypeIdentifier(sbdh.getProfileTypeIdentifier());
        peppolTransmissionMetaData.setSendingAccessPointId(new AccessPointIdentifier(as2Message.getAs2From()));
        peppolTransmissionMetaData.setReceivingAccessPoint(new AccessPointIdentifier(as2Message.getAs2To()));

        // Retrieves the Common Name of the X500Principal, which is used to construct the AccessPointIdentifier for the senders access point
        X500Principal subjectX500Principal = as2Message.getSignedMimeMessage().getSignersX509Certificate().getSubjectX500Principal();
        peppolTransmissionMetaData.setSendingAccessPointPrincipal(subjectX500Principal);

        return peppolTransmissionMetaData;

    }

    private SMimeDigestMethod inspectDispositionNotificationOptions(InternetHeaders internetHeaders)
            throws MdnRequestException {
        String[] value = internetHeaders.getHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS);

        if (value == null)
            throw new MdnRequestException(String.format("AS2 header '%s' not found in request.",
                    As2Header.DISPOSITION_NOTIFICATION_OPTIONS));

        // Attempts to parseMultipart the Disposition Notification Options
        As2DispositionNotificationOptions as2DispositionNotificationOptions = As2DispositionNotificationOptions.valueOf(value[0]);
        String micAlgorithm = as2DispositionNotificationOptions.getPreferredSignedReceiptMicAlgorithmName();

        SMimeDigestMethod sMimeDigestMethod = SMimeDigestMethod.findByIdentifier(micAlgorithm);
        if (sMimeDigestMethod.getTransportProfile() == null)
            throw new MdnRequestException(String.format("MIC algorithm '%s' not recognized.", micAlgorithm));

        return sMimeDigestMethod;
    }
}