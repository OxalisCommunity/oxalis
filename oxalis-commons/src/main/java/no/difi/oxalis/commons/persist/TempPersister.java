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

package no.difi.oxalis.commons.persist;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.lang.EvidenceException;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.vefa.peppol.common.model.Header;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static no.difi.oxalis.commons.filesystem.FileUtils.filterString;

/**
 * @author erlend
 * @since 4.0.0
 */
public class TempPersister implements PayloadPersister, ReceiptPersister {

    private final EvidenceFactory evidenceFactory;

    private final Path folder;

    @Inject
    public TempPersister(EvidenceFactory evidenceFactory) throws IOException {
        this.evidenceFactory = evidenceFactory;
        this.folder = Files.createTempDirectory("oxalis-inbound");
    }

    @Override
    public Path persist(MessageId messageId, Header header, InputStream inputStream) throws IOException {
        // Create temp file
        Path path = getFolder(header)
                .resolve(String.format("%s.xml", filterString(messageId.stringValue())));

        // Copy content to temp file
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            ByteStreams.copy(inputStream, outputStream);
        }

        // Return file name
        return path;
    }

    @Override
    public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
        // Create temp file
        Path path = getFolder(inboundMetadata.getHeader()).resolve(
                String.format("%s.evidence.dat", filterString(inboundMetadata.getMessageId().stringValue())));

        // Copy content to temp file
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            evidenceFactory.write(outputStream, inboundMetadata);
        } catch (EvidenceException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private Path getFolder(Header header) throws IOException {
        // Initiate folder to be used.
        Path path = Paths.get(
                folder.toString(),
                filterString(header.getReceiver().getIdentifier()),
                filterString(header.getSender().getIdentifier()));

        // Create and return folder.
        return Files.createDirectories(path);
    }
}
