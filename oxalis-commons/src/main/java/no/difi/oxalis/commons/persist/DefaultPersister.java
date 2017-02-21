package no.difi.oxalis.commons.persist;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.lang.EvidenceException;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.commons.filesystem.FileUtils;
import no.difi.vefa.peppol.common.model.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DefaultPersister implements PayloadPersister, ReceiptPersister {

    public static final Logger LOGGER = LoggerFactory.getLogger(DefaultPersister.class);

    private final EvidenceFactory evidenceFactory;

    private final Path inboundFolder;

    @Inject
    public DefaultPersister(@Named("inbound") Path inboundFolder, EvidenceFactory evidenceFactory) {
        this.inboundFolder = inboundFolder;
        this.evidenceFactory = evidenceFactory;
    }

    @Override
    public Path persist(TransmissionIdentifier transmissionIdentifier, Header header, InputStream inputStream)
            throws IOException {
        Path path = getFolder(header).resolve(
                String.format("%s.-doc.xml", FileUtils.filterString(transmissionIdentifier.getValue())));

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            ByteStreams.copy(inputStream, outputStream);
        }

        LOGGER.debug("Payload persisted to: {}", path);

        return path;
    }

    @Override
    public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
        Path path = getFolder(inboundMetadata.getHeader()).resolve(
                String.format("%s.receipt.dat.",
                        FileUtils.filterString(inboundMetadata.getTransmissionIdentifier().getValue())));

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            evidenceFactory.write(outputStream, inboundMetadata);
        } catch (EvidenceException e) {
            throw new IOException("Unable to persist receipt.", e);
        }

        LOGGER.debug("Receipt persisted to: {}", path);
    }

    protected Path getFolder(Header header) throws IOException {
        Path folder = inboundFolder.resolve(Paths.get(
                FileUtils.filterString(header.getReceiver().getIdentifier()),
                FileUtils.filterString(header.getSender().getIdentifier())));

        Files.createDirectories(folder);

        return folder;
    }
}
