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
