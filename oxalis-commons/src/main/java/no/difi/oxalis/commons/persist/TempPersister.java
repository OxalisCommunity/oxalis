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
import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.commons.filesystem.FileUtils;
import no.difi.vefa.peppol.common.model.Header;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author erlend
 * @since 4.0.0
 */
public class TempPersister implements PayloadPersister, ReceiptPersister {

    private Path folder;

    public TempPersister() throws IOException {
        folder = Files.createTempDirectory("oxalis-inbound");
    }

    @Override
    public Path persist(MessageId messageId, Header header, InputStream inputStream) throws IOException {
        // Create temp folder
        Files.createDirectories(Paths.get(
                folder.toString(),
                FileUtils.filterString(header.getReceiver().getIdentifier()),
                FileUtils.filterString(header.getSender().getIdentifier())));

        // Create temp file
        Path path = Paths.get(
                folder.toString(),
                FileUtils.filterString(header.getReceiver().getIdentifier()),
                FileUtils.filterString(header.getSender().getIdentifier()),
                String.format("%s.xml", FileUtils.filterString(messageId.stringValue())));

        // Copy content to temp file
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            ByteStreams.copy(inputStream, outputStream);
        }

        // Return file name
        return path;
    }

    @Override
    public Path persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
        // Create temp file
        Files.createDirectories(Paths.get(
                folder.toString(),
                FileUtils.filterString(inboundMetadata.getHeader().getReceiver().getIdentifier()),
                FileUtils.filterString(inboundMetadata.getHeader().getSender().getIdentifier())));

        Path path = Paths.get(
                folder.toString(),
                FileUtils.filterString(inboundMetadata.getHeader().getReceiver().getIdentifier()),
                FileUtils.filterString(inboundMetadata.getHeader().getSender().getIdentifier()),
                String.format("%s.mdn.dat", FileUtils.filterString(inboundMetadata.getMessageId().stringValue())));

        // Copy content to temp file
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            // TODO ByteStreams.copy(inputStream, outputStream);
        }

        // Return file name
        return path;
    }
}
