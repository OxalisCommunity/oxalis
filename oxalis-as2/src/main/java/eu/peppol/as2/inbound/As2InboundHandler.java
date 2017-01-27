/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.peppol.as2.inbound;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import eu.peppol.as2.lang.InvalidAs2MessageException;
import eu.peppol.as2.model.MdnData;
import eu.peppol.as2.model.Mic;
import eu.peppol.as2.util.*;
import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.inbound.InboundVerifier;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.api.statistics.StatisticsService;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.commons.bouncycastle.BCHelper;
import no.difi.oxalis.commons.io.PeekingInputStream;
import no.difi.oxalis.commons.io.UnclosableInputStream;
import no.difi.vefa.peppol.common.code.Service;
import no.difi.vefa.peppol.common.model.Digest;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.sbdh.SbdReader;
import no.difi.vefa.peppol.security.api.CertificateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;

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

    private final StatisticsService statisticsService;

    private final TimestampProvider timestampProvider;

    private final PayloadPersister payloadPersister;

    private final ReceiptPersister receiptPersister;

    private final InboundVerifier inboundVerifier;

    private final CertificateValidator certificateValidator;

    private Header header;

    private Digest calculatedDigest;

    @Inject
    public As2InboundHandler(MdnMimeMessageFactory mdnMimeMessageFactory,
                             StatisticsService statisticsService, TimestampProvider timestampProvider,
                             CertificateValidator certificateValidator,
                             PayloadPersister payloadPersister, ReceiptPersister receiptPersister,
                             InboundVerifier inboundVerifier) {
        this.mdnMimeMessageFactory = mdnMimeMessageFactory;
        this.statisticsService = statisticsService;
        this.timestampProvider = timestampProvider;
        this.certificateValidator = certificateValidator;

        this.payloadPersister = payloadPersister;
        this.receiptPersister = receiptPersister;
        this.inboundVerifier = inboundVerifier;
    }

    /**
     * Receives an AS2 Message in the form of a map of headers together with the payload,
     * which is made available in an input stream
     * <p>
     * If persisting message to the Message Repository fails, we have to return negative MDN.
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

                // Initiate MDN
                MdnBuilder mdnBuilder = MdnBuilder.newInstance(mimeMessage);
                mdnBuilder.addHeader(MdnHeader.DATE, t2.getDate());

                // Extract Message-ID
                MessageId messageId = new MessageId(httpHeaders.getHeader(As2Header.MESSAGE_ID)[0]);
                mdnBuilder.addHeader(MdnHeader.ORIGINAL_MESSAGE_ID, messageId.stringValue());

                // Extract signed digest and digest algorithm
                SMimeDigestMethod digestMethod = sMimeReader.getDigestMethod();

                // Extract content headers
                byte[] headerBytes = sMimeReader.getBodyHeader();
                mdnBuilder.addHeader(MdnHeader.ORIGINAL_CONTENT_HEADER, headerBytes);

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

                // Extract "fresh" InputStream
                Path payloadPath;
                try (InputStream payloadInputStream = peekingInputStream.newInputStream()) {

                    // Persist content
                    payloadPath =
                            payloadPersister.persist(messageId, header, new UnclosableInputStream(payloadInputStream));

                    // Exhaust InputStream
                    ByteStreams.exhaust(payloadInputStream);
                }

                // Fetch calculated digest
                calculatedDigest = Digest.of(digestMethod.getDigestMethod(), messageDigest.digest());
                mdnBuilder.addHeader(MdnHeader.RECEIVED_CONTENT_MIC, new Mic(calculatedDigest));

                // Validate signature using calculated digest
                X509Certificate signer = SMimeBC.verifySignature(
                        ImmutableMap.of(digestMethod.getOid(), calculatedDigest.getValue()),
                        sMimeReader.getSignature()
                );

                // Validate certificate
                certificateValidator.validate(Service.AP, signer);

                // TODO Create receipt (MDN)
                // mdnBuilder.build();

                // Persist metadata
                As2InboundMetadata inboundMetadata = new As2InboundMetadata(
                        messageId, header, t2, digestMethod.getTransportProfile(), calculatedDigest, signer);
                receiptPersister.persist(inboundMetadata, payloadPath);

                // Persist statistics
                statisticsService.persist(inboundMetadata);

            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                throw new IllegalStateException("Error during handling.", e);
            }

            // Creates the MDN data to be returned (not the actual MDN, which must be represented as an S/MIME message)
            // Calculates the MIC for the payload using the preferred mic algorithm
            // String micAlgorithmName = as2Message.getDispositionNotificationOptions().getPreferredSignedReceiptMicAlgorithmName();
            // Mic mic = as2Message.getSignedMimeMessage().calculateMic(micAlgorithmName);
            // log.info("Calculated MIC (old) : {}", mic);
            // MdnData mdnData = createMdnData(httpHeaders, mic);
            MdnData mdnData = createMdnData(httpHeaders, new Mic(calculatedDigest));

            // Finally we persist the raw statistics data
            // persistStatistics(peppolTransmissionMetaData);

            // Grabs the S/MIME message to be returned to the sender
            MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(mdnData, httpHeaders);

            // Returns the response to be emitted by whoever is calling us
            return new ResponseData(HttpServletResponse.SC_OK, signedMdn, mdnData);
        } catch (InvalidAs2MessageException | MessagingException e) {
            log.error("Invalid AS2 message: " + e.getMessage(), e);

            MdnData mdnData = MdnData.Builder.buildFailureFromHeaders(httpHeaders, new Mic(calculatedDigest), e.getMessage());
            MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(mdnData, httpHeaders);

            return new ResponseData(HttpServletResponse.SC_BAD_REQUEST, signedMdn, mdnData);
        }
    }

    protected MdnData createMdnData(InternetHeaders internetHeaders, Mic mic) {
        MdnData mdnData = MdnData.Builder.buildProcessedOK(internetHeaders, mic);
        log.debug("Message received OK, MDN returned will be: " + mdnData);
        return mdnData;
    }

}