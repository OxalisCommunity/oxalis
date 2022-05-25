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

package network.oxalis.commons.persist;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.evidence.EvidenceFactory;
import network.oxalis.api.inbound.InboundMetadata;
import network.oxalis.api.lang.EvidenceException;
import network.oxalis.api.model.TransmissionIdentifier;
import network.oxalis.api.persist.PersisterHandler;
import network.oxalis.api.util.Type;
import network.oxalis.commons.filesystem.FileUtils;
import network.oxalis.vefa.peppol.common.model.Header;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author erlend
 * @since 4.0.0
 */
@Singleton
@Type("temp")
@Slf4j
public class TempPersister implements PersisterHandler {

    private final EvidenceFactory evidenceFactory;

    private final Path folder;

    @Inject
    public TempPersister(EvidenceFactory evidenceFactory) throws IOException {
        this.evidenceFactory = evidenceFactory;
        this.folder = Files.createTempDirectory("oxalis-inbound");
    }

    @Override
    public Path persist(TransmissionIdentifier transmissionIdentifier, Header header, InputStream inputStream)
            throws IOException {
        // Create temp file
        Path path = PersisterUtils.createArtifactFolders(folder, header)
                .resolve(String.format("%s.xml", FileUtils.filterString(transmissionIdentifier.getIdentifier())));

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
        Path path = PersisterUtils.createArtifactFolders(folder, inboundMetadata.getHeader()).resolve(
                String.format("%s.evidence.dat",
                        FileUtils.filterString(inboundMetadata.getTransmissionIdentifier().getIdentifier())));

        // Copy content to temp file
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            evidenceFactory.write(outputStream, inboundMetadata);
        } catch (EvidenceException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * @since 4.0.3
     */
    @Override
    public void persist(TransmissionIdentifier transmissionIdentifier, Header header,
                        Path payloadPath, Exception exception) {
        try {
            // Delete temp file
            if (Files.exists(payloadPath))
                Files.delete(payloadPath);
        } catch (IOException e) {
            log.warn("Unable to delete temp file: {}", payloadPath, e);
        }
    }
}
