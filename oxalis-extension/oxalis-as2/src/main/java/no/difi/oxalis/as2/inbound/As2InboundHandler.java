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
import io.opentracing.Span;
import no.difi.oxalis.api.header.HeaderParser;
import no.difi.oxalis.api.inbound.InboundService;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.lang.OxalisSecurityException;
import no.difi.oxalis.api.lang.VerifierException;
import no.difi.oxalis.api.model.Direction;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.tag.Tag;
import no.difi.oxalis.api.tag.TagGenerator;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.api.transmission.TransmissionVerifier;
import no.difi.oxalis.as2.api.MessageIdGenerator;
import no.difi.oxalis.as2.code.As2Header;
import no.difi.oxalis.as2.code.Disposition;
import no.difi.oxalis.as2.code.MdnHeader;
import no.difi.oxalis.as2.lang.OxalisAs2InboundException;
import no.difi.oxalis.as2.model.Mic;
import no.difi.oxalis.as2.util.*;
import no.difi.oxalis.commons.bouncycastle.BCHelper;
import no.difi.oxalis.commons.io.PeekingInputStream;
import no.difi.oxalis.commons.io.UnclosableInputStream;
import no.difi.oxalis.commons.mode.OxalisCertificateValidator;
import no.difi.vefa.peppol.common.code.Service;
import no.difi.vefa.peppol.common.model.Digest;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
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

    private final InboundService inboundService;

    private final TimestampProvider timestampProvider;

    private final PersisterHandler persisterHandler;

    private final TransmissionVerifier transmissionVerifier;

    private final OxalisCertificateValidator certificateValidator;

    private final SMimeMessageFactory sMimeMessageFactory;

    private final TagGenerator tagGenerator;

    private final MessageIdGenerator messageIdGenerator;

    private final HeaderParser headerParser;

    @Inject
    public As2InboundHandler(InboundService inboundService, TimestampProvider timestampProvider,
                             OxalisCertificateValidator certificateValidator, PersisterHandler persisterHandler,
                             TransmissionVerifier transmissionVerifier, SMimeMessageFactory sMimeMessageFactory,
                             TagGenerator tagGenerator, MessageIdGenerator messageIdGenerator,
                             HeaderParser headerParser) {
        this.inboundService = inboundService;
        this.timestampProvider = timestampProvider;
        this.certificateValidator = certificateValidator;

        this.persisterHandler = persisterHandler;
        this.transmissionVerifier = transmissionVerifier;

        this.sMimeMessageFactory = sMimeMessageFactory;

        this.tagGenerator = tagGenerator;
        this.messageIdGenerator = messageIdGenerator;
        this.headerParser = headerParser;
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
    public MimeMessage receive(InternetHeaders httpHeaders, MimeMessage mimeMessage, Span root) throws OxalisAs2InboundException {
        TransmissionIdentifier transmissionIdentifier = null;
        Header header = null;
        Path payloadPath = null;
        OxalisAs2InboundException exception = null;

        try {
            SMimeReader sMimeReader = new SMimeReader(mimeMessage);

            // Get timestamp using signature as input
            Timestamp t2 = timestampProvider.generate(sMimeReader.getSignature(), Direction.IN);

            Tag tag = tagGenerator.generate(Direction.IN);

            // Initiate MDN
            MdnBuilder mdnBuilder = MdnBuilder.newInstance(mimeMessage);
            mdnBuilder.addHeader(MdnHeader.DATE, t2.getDate());

            // Extract Message-ID
            transmissionIdentifier = TransmissionIdentifier.fromHeader(httpHeaders.getHeader(As2Header.MESSAGE_ID)[0]);
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

            // Prepare content for reading of header
            PeekingInputStream peekingInputStream = new PeekingInputStream(digestInputStream);

            // Extract header
            header = headerParser.parse(peekingInputStream);

            // Perform validation of header
            transmissionVerifier.verify(header, Direction.IN);

            // Extract "fresh" InputStream
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
            certificateValidator.validate(Service.AP, signer, root);

            // Generate Message-Id
            String messageId = messageIdGenerator.generate(new As2InboundMetadata(transmissionIdentifier, header, t2,
                    null, null, signer, null, tag));

            if (!MessageIdUtil.verify(messageId))
                throw new OxalisAs2InboundException(
                        "Invalid Message-ID '" + messageId + "' generated.",
                        Disposition.UNEXPECTED_PROCESSING_ERROR);

            // Create receipt (MDN)
            mdnBuilder.addHeader(MdnHeader.DISPOSITION, Disposition.PROCESSED);
            MimeMessage mdn = sMimeMessageFactory.createSignedMimeMessage(mdnBuilder.build(), digestMethod);
            // MimeMessage mdn = sMimeMessageFactory.createSignedMimeMessageNew(mdnBuilder.build(), calculatedDigest, digestMethod);
            mdn.setHeader(As2Header.MESSAGE_ID, messageId);
            mdn.setHeader(As2Header.AS2_VERSION, As2Header.VERSION);
            mdn.setHeader(As2Header.AS2_FROM, httpHeaders.getHeader(As2Header.AS2_TO)[0]);
            mdn.setHeader(As2Header.AS2_TO, httpHeaders.getHeader(As2Header.AS2_FROM)[0]);

            // Prepare MDN
            ByteArrayOutputStream mdnOutputStream = new ByteArrayOutputStream();
            mdn.writeTo(mdnOutputStream);

            // Persist metadata
            As2InboundMetadata inboundMetadata = new As2InboundMetadata(transmissionIdentifier, header, t2,
                    digestMethod.getTransportProfile(), calculatedDigest, signer, mdnOutputStream.toByteArray(), tag);
            persisterHandler.persist(inboundMetadata, payloadPath);

            // Persist statistics
            inboundService.complete(inboundMetadata);

            return mdn;
        } catch (OxalisContentException e) {
            exception = new OxalisAs2InboundException(Disposition.UNSUPPORTED_FORMAT, e.getMessage(), e);
            persisterHandler.persist(transmissionIdentifier, header, payloadPath, exception);
            throw exception;
        } catch (NoSuchAlgorithmException e) {
            exception = new OxalisAs2InboundException(Disposition.UNSUPPORTED_MIC_ALGORITHMS, e.getMessage(), e);
            persisterHandler.persist(transmissionIdentifier, header, payloadPath, exception);
            throw exception;
        } catch (VerifierException e) {
            exception = new OxalisAs2InboundException(Disposition.fromVerifierException(e), e.getMessage(), e);
            persisterHandler.persist(transmissionIdentifier, header, payloadPath, exception);
            throw exception;
        } catch (PeppolSecurityException e) {
            exception = new OxalisAs2InboundException(Disposition.AUTHENTICATION_FAILED, e.getMessage(), e);
            persisterHandler.persist(transmissionIdentifier, header, payloadPath, exception);
            throw exception;
        } catch (OxalisSecurityException e) {
            exception = new OxalisAs2InboundException(Disposition.INTEGRITY_CHECK_FAILED, e.getMessage(), e);
            persisterHandler.persist(transmissionIdentifier, header, payloadPath, exception);
            throw exception;
        } catch (Exception e) {
            exception = new OxalisAs2InboundException(Disposition.UNEXPECTED_PROCESSING_ERROR, e.getMessage(), e);
            persisterHandler.persist(transmissionIdentifier, header, payloadPath, exception);
            throw exception;
        }
    }
}
