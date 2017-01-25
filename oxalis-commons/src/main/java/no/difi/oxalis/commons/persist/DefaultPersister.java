package no.difi.oxalis.commons.persist;

import com.google.common.io.ByteStreams;
import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.persist.PayloadPersister;
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
public class DefaultPersister implements PayloadPersister, ReceiptPersister {

    @Override
    public Path persist(MessageId messageId, Header header, InputStream inputStream) throws IOException {
        Path folder = Paths.get(
                "inbound",
                FileUtils.filterString(header.getReceiver().getIdentifier()),
                FileUtils.filterString(header.getSender().getIdentifier()));

        Files.createDirectories(folder);

        Path path = Paths.get(
                "inbound",
                FileUtils.filterString(header.getReceiver().getIdentifier()),
                FileUtils.filterString(header.getSender().getIdentifier()),
                String.format("%s.xml", FileUtils.filterString(messageId.stringValue())));

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            ByteStreams.copy(inputStream, outputStream);
        }

        return path;
    }

    @Override
    public Path persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
        return null;
    }
}
