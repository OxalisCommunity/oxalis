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

package no.difi.oxalis.as2.inbound;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import no.difi.oxalis.api.lang.OxalisSecurityException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.lang.TimestampException;
import no.difi.oxalis.api.lang.VerifierException;
import no.difi.oxalis.api.model.Direction;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.statistics.StatisticsService;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.api.transmission.TransmissionVerifier;
import no.difi.oxalis.as2.code.As2Header;
import no.difi.oxalis.as2.code.Disposition;
import no.difi.oxalis.as2.code.MdnHeader;
import no.difi.oxalis.as2.lang.OxalisAs2InboundException;
import no.difi.oxalis.as2.model.Mic;
import no.difi.oxalis.as2.util.*;
import no.difi.oxalis.commons.bouncycastle.BCHelper;
import no.difi.oxalis.commons.io.PeekingInputStream;
import no.difi.oxalis.commons.io.UnclosableInputStream;
import no.difi.vefa.peppol.common.code.Service;
import no.difi.vefa.peppol.common.model.Digest;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.sbdh.SbdReader;
import no.difi.vefa.peppol.sbdh.lang.SbdhException;
import no.difi.vefa.peppol.security.api.CertificateValidator;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Main entry point for receiving AS2 messages.
 *
 * @author steinar
 * @author thore
 * @author erlend
 */
class As2InboundHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger(As2InboundHandler.class);

    private final StatisticsService statisticsService;

    private final TimestampProvider timestampProvider;

    private final PersisterHandler persisterHandler;

    private final TransmissionVerifier transmissionVerifier;

    private final CertificateValidator certificateValidator;

    private final SMimeMessageFactory sMimeMessageFactory;

    @Inject
    public As2InboundHandler(StatisticsService statisticsService, TimestampProvider timestampProvider,
                             CertificateValidator certificateValidator, PersisterHandler persisterHandler,
                             TransmissionVerifier transmissionVerifier, SMimeMessageFactory sMimeMessageFactory) {
        this.statisticsService = statisticsService;
        this.timestampProvider = timestampProvider;
        this.certificateValidator = certificateValidator;

        this.persisterHandler = persisterHandler;
        this.transmissionVerifier = transmissionVerifier;

        this.sMimeMessageFactory = sMimeMessageFactory;
    }

    /**
     * Receives an AS2 Message in the form of a map of headers together with the payload,
     * which is made available in an input stream
     * <p>
     * If persisting message to the Message Repository fails, we have to return negative MDN.
     *
     * @param httpHeaders the http headers received
     * @param mimeMessage supplies the MIME message
     * @return MDN object to signal if everything is ok or if some error occurred while receiving
     */
    public MimeMessage receive(InternetHeaders httpHeaders, MimeMessage mimeMessage) throws OxalisAs2InboundException {
        LOGGER.debug("Receiving message ..");

        try {
            SMimeReader sMimeReader = new SMimeReader(mimeMessage);

            // Get timestamp using signature as input
            Timestamp t2 = timestampProvider.generate(sMimeReader.getSignature(), Direction.IN);

            // Initiate MDN
            MdnBuilder mdnBuilder = MdnBuilder.newInstance(mimeMessage);
            mdnBuilder.addHeader(MdnHeader.DATE, t2.getDate());

            // Extract Message-ID
            TransmissionIdentifier transmissionIdentifier =
                    TransmissionIdentifier.fromHeader(httpHeaders.getHeader(As2Header.MESSAGE_ID)[0]);
            mdnBuilder.addHeader(MdnHeader.ORIGINAL_MESSAGE_ID, httpHeaders.getHeader(As2Header.MESSAGE_ID)[0]);

            // Extract signed digest and digest algorithm
            SMimeDigestMethod digestMethod = sMimeReader.getDigestMethod();

            // Extract content headers
            byte[] headerBytes = sMimeReader.getBodyHeader();
            mdnBuilder.addHeader(MdnHeader.ORIGINAL_CONTENT_HEADER, headerBytes);

            // Prepare calculation of digest
            MessageDigest messageDigest = BCHelper.getMessageDigest(digestMethod.getIdentifier());
            InputStream digestInputStream = new DigestInputStream(sMimeReader.getBodyInputStream(), messageDigest);

            // Add header to calculation of digest
            messageDigest.update(headerBytes);

            // Prepare content for reading of SBDH
            PeekingInputStream peekingInputStream = new PeekingInputStream(digestInputStream);

            // Extract SBDH
            Header header;
            try (SbdReader sbdReader = SbdReader.newInstance(peekingInputStream)) {
                header = sbdReader.getHeader();
            }

            // Perform validation of SBDH
            transmissionVerifier.verify(header, Direction.IN);

            // Extract "fresh" InputStream
            Path payloadPath;
            try (InputStream payloadInputStream = peekingInputStream.newInputStream()) {

                // Persist content
                payloadPath = persisterHandler.persist(transmissionIdentifier, header,
                        new UnclosableInputStream(payloadInputStream));

                // Exhaust InputStream
                ByteStreams.exhaust(payloadInputStream);
            }

            // Fetch calculated digest
            Digest calculatedDigest = Digest.of(digestMethod.getDigestMethod(), messageDigest.digest());
            mdnBuilder.addHeader(MdnHeader.RECEIVED_CONTENT_MIC, new Mic(calculatedDigest));

            // Validate signature using calculated digest
            X509Certificate signer = SMimeBC.verifySignature(
                    ImmutableMap.of(digestMethod.getOid(), calculatedDigest.getValue()),
                    sMimeReader.getSignature()
            );

            // Validate certificate
            certificateValidator.validate(Service.AP, signer);

            // Create receipt (MDN)
            mdnBuilder.addHeader(MdnHeader.DISPOSITION, Disposition.PROCESSED);
            MimeMessage mdn = sMimeMessageFactory.createSignedMimeMessage(mdnBuilder.build(), digestMethod);
            // MimeMessage mdn = sMimeMessageFactory.createSignedMimeMessageNew(mdnBuilder.build(), calculatedDigest, digestMethod);
            mdn.setHeader(As2Header.AS2_VERSION, As2Header.VERSION);
            mdn.setHeader(As2Header.AS2_FROM, httpHeaders.getHeader(As2Header.AS2_TO)[0]);
            mdn.setHeader(As2Header.AS2_TO, httpHeaders.getHeader(As2Header.AS2_FROM)[0]);

            // Prepare MDN
            ByteArrayOutputStream mdnOutputStream = new ByteArrayOutputStream();
            mdn.writeTo(mdnOutputStream);

            // Persist metadata
            As2InboundMetadata inboundMetadata = new As2InboundMetadata(transmissionIdentifier, header, t2,
                    digestMethod.getTransportProfile(), calculatedDigest, signer, mdnOutputStream.toByteArray());
            persisterHandler.persist(inboundMetadata, payloadPath);

            // Persist statistics
            statisticsService.persist(inboundMetadata);

            return mdn;
        } catch (SbdhException e) {
            throw new OxalisAs2InboundException(Disposition.UNSUPPORTED_FORMAT, e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new OxalisAs2InboundException(Disposition.UNSUPPORTED_MIC_ALGORITHMS, e.getMessage(), e);
        } catch (VerifierException e) {
            throw new OxalisAs2InboundException(Disposition.fromVerifierException(e), e.getMessage(), e);
        } catch (PeppolSecurityException e) {
            throw new OxalisAs2InboundException(Disposition.AUTHENTICATION_FAILED, e.getMessage(), e);
        } catch (OxalisSecurityException e) {
            throw new OxalisAs2InboundException(Disposition.INTEGRITY_CHECK_FAILED, e.getMessage(), e);
        } catch (IOException | TimestampException | MessagingException | OxalisTransmissionException e) {
            throw new OxalisAs2InboundException(Disposition.UNEXPECTED_PROCESSING_ERROR, e.getMessage(), e);
        }
    }
}
