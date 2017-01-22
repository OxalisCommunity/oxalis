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

import com.google.inject.Inject;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.PeppolTransmissionMetaData;
import eu.peppol.as2.lang.InvalidAs2MessageException;
import eu.peppol.as2.lang.MdnRequestException;
import eu.peppol.as2.model.As2Message;
import eu.peppol.as2.model.MdnData;
import eu.peppol.as2.model.Mic;
import eu.peppol.as2.util.*;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.identifier.MessageId;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.OxalisMessagePersistenceException;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import no.difi.oxalis.api.inbound.ContentPersister;
import no.difi.oxalis.api.inbound.InboundVerifier;
import no.difi.oxalis.api.inbound.ReceiptPersister;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.commons.bouncycastle.BCHelper;
import no.difi.oxalis.commons.io.PeekingInputStream;
import no.difi.vefa.peppol.common.model.Digest;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.sbdh.SbdReader;
import no.difi.vefa.peppol.security.api.CertificateValidator;
import no.difi.vefa.peppol.security.util.EmptyCertificateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

/**
 * Main entry point for receiving AS2 messages.
 *
 * @author steinar
 * @author thore
 * @author erlend
 */
class As2InboundHandler {

    public static final Logger log = LoggerFactory.getLogger(As2InboundHandler.class);

    private final MdnMimeMessageFactory mdnMimeMessageFactory;

    private final MessageRepository messageRepository;

    private final RawStatisticsRepository rawStatisticsRepository;

    private final AccessPointIdentifier ourAccessPointIdentifier;

    private final TimestampProvider timestampProvider;

    private final ContentPersister contentPersister;

    private final ReceiptPersister receiptPersister;

    private final InboundVerifier inboundVerifier;

    private final CertificateValidator certificateValidator = EmptyCertificateValidator.INSTANCE;

    private Header header;

    private Digest calculatedDigest;

    @Inject
    public As2InboundHandler(MdnMimeMessageFactory mdnMimeMessageFactory, MessageRepository messageRepository,
                             RawStatisticsRepository rawStatisticsRepository, TimestampProvider timestampProvider,
                             AccessPointIdentifier ourAccessPointIdentifier, ContentPersister contentPersister,
                             ReceiptPersister receiptPersister, InboundVerifier inboundVerifier) {
        this.mdnMimeMessageFactory = mdnMimeMessageFactory;
        this.messageRepository = messageRepository;
        this.rawStatisticsRepository = rawStatisticsRepository;
        this.ourAccessPointIdentifier = ourAccessPointIdentifier;
        this.timestampProvider = timestampProvider;

        this.contentPersister = contentPersister;
        this.receiptPersister = receiptPersister;
        this.inboundVerifier = inboundVerifier;
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

        try {
            log.debug("Receiving message ..");

            log.debug("Message contains valid AS2 Disposition-notification-options, now creating internal AS2 message...");

            MimeMessage mimeMessage = MimeMessageHelper.createMimeMessageAssistedByHeaders(inputStream, httpHeaders);


            try (SMimeReader sMimeReader = new SMimeReader(mimeMessage)) {
                // Get timestamp using signature as input
                Timestamp t2 = timestampProvider.generate(sMimeReader.getSignature());

                // Extract Message-ID
                MessageId messageId = new MessageId(httpHeaders.getHeader(As2Header.MESSAGE_ID)[0]);

                // Extract signed digest and digest algorithm
                SMimeDigestMethod digestMethod = sMimeReader.getDigestMethod();

                // Extract content headers
                byte[] headerBytes = sMimeReader.getBodyHeader();

                // Prepare calculation of digest
                MessageDigest messageDigest = BCHelper.getMessageDigest(digestMethod.getMethod());
                InputStream digestInputStream = new DigestInputStream(sMimeReader.getBodyInputStream(), messageDigest);

                // Add header to calculation of digest
                messageDigest.update(headerBytes);

                // Prepare content for reading of SBDH
                PeekingInputStream peekingInputStream = new PeekingInputStream(digestInputStream);

                // Extract SBDH
                try (SbdReader sbdReader = SbdReader.newInstance(peekingInputStream)) {
                    header = sbdReader.getHeader();
                }

                // Perform validation of SBDH
                inboundVerifier.verify(messageId, header);

                // Persist content
                contentPersister.persist(messageId, header, peekingInputStream.newInputStream());

                // Fetch calculated digest
                calculatedDigest = Digest.of(digestMethod.getDigestMethod(), messageDigest.digest());

                // TODO Validate signature using calculated digest
                /*
                log.info(Base64.getEncoder().encodeToString(calculatedDigest.getValue()));

                Map<ASN1ObjectIdentifier, byte[]> hashes = new HashMap<>();
                hashes.put(digestMethod.getOid(), calculatedDigest.getValue());

                X509Certificate signer = SMimeBC.verifySignature(
                        hashes,
                        // ImmutableMap.of(digestMethod.getOid(), calculatedDigest.getValue()),
                        sMimeReader.getSignature()
                );
                */
                // log.info(Base64.getEncoder().encodeToString(sMimeReader.getSignature()));

                // TODO Validate certificate
                // certificateValidator.validate(Service.AP, signer);

                // TODO Create receipt (MDN)

                // Persist metadata
                As2InboundMetadata inboundMetadata = new As2InboundMetadata(
                        messageId, header, t2, digestMethod.getTransportProfile(), calculatedDigest);
                receiptPersister.persist(inboundMetadata);

                // TODO Persist statistics

            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                throw new IllegalStateException("Error during handling.", e);
            }

            log.info("Calculated MIC (new) : {}", new Mic(calculatedDigest));

            SignedMimeMessage signedMimeMessage = new SignedMimeMessage(mimeMessage);
            log.debug("MIME message converted to S/MIME message");

            // Transforms the input data into a proper As2Message
            As2Message as2Message = As2MessageFactory.createAs2MessageFrom(httpHeaders, signedMimeMessage);

            // Validates the message headers according to the PEPPOL rules and performs semantic validation
            log.debug("Validating AS2 Message: " + as2Message);
            As2MessageInspector.validate(as2Message);

            // Extracts the SBDH from the message, the SBDH is required by OpenPEPPOL.
            // Throws IllegalStateException if anything goes wrong.
            PeppolStandardBusinessHeader sbdh = new PeppolStandardBusinessHeader(header);

            // Persists the payload
            PeppolTransmissionMetaData peppolTransmissionMetaData = persistPayload(sbdh, as2Message);

            // Creates the MDN data to be returned (not the actual MDN, which must be represented as an S/MIME message)
            // Calculates the MIC for the payload using the preferred mic algorithm
            String micAlgorithmName = as2Message.getDispositionNotificationOptions().getPreferredSignedReceiptMicAlgorithmName();
            Mic mic = as2Message.getSignedMimeMessage().calculateMic(micAlgorithmName);
            log.info("Calculated MIC (old) : {}", mic);
            MdnData mdnData = createMdnData(httpHeaders, mic);

            // Finally we persist the raw statistics data
            persistStatistics(peppolTransmissionMetaData);

            // Grabs the S/MIME message to be returned to the sender
            MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(mdnData, httpHeaders);

            // Returns the response to be emitted by whoever is calling us
            return new ResponseData(HttpServletResponse.SC_OK, signedMdn, mdnData);
        } catch (InvalidAs2MessageException | MdnRequestException | OxalisMessagePersistenceException | MessagingException e) {
            log.error("Invalid AS2 message: " + e.getMessage(), e);

            MdnData mdnData = MdnData.Builder.buildFailureFromHeaders(httpHeaders, new Mic(calculatedDigest), e.getMessage());
            MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(mdnData, httpHeaders);

            return new ResponseData(HttpServletResponse.SC_BAD_REQUEST, signedMdn, mdnData);
        }
    }

    protected PeppolTransmissionMetaData persistPayload(PeppolStandardBusinessHeader sbdh, As2Message as2Message) throws OxalisMessagePersistenceException {

        log.debug("Persisting AS2 Message ....");

        long start = System.nanoTime();
        PeppolTransmissionMetaData peppolTransmissionMetaData = collectTransmissionMetaData(as2Message, sbdh);

        // Performs the actual persistence by invoking whatever has been configured for persistence
        messageRepository.saveInboundMessage(peppolTransmissionMetaData, as2Message.getSignedMimeMessage().getPayload());

        long elapsed = System.nanoTime() - start;
        log.debug("Persistence of payload took " + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS) + "ms");

        return peppolTransmissionMetaData;
    }

    protected MdnData createMdnData(InternetHeaders internetHeaders, Mic mic) {
        MdnData mdnData = MdnData.Builder.buildProcessedOK(internetHeaders, mic);
        log.debug("Message received OK, MDN returned will be: " + mdnData);
        return mdnData;
    }

    protected void persistStatistics(PeppolTransmissionMetaData peppolTransmissionMetaData) {
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
}