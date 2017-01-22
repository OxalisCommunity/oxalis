package no.difi.oxalis.inbound.persister;

import com.google.common.io.ByteStreams;
import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.inbound.ContentPersister;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.inbound.ReceiptPersister;
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
public class DefaultPersister implements ContentPersister, ReceiptPersister {

    @Override
    public Path persist(MessageId messageId, Header header, InputStream inputStream) throws IOException {
        Path path = Paths.get(
                "inbound",
                header.getReceiver().getIdentifier(),
                header.getSender().getIdentifier(),
                String.format("%s.xml", messageId.stringValue())
        );

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            ByteStreams.copy(inputStream, outputStream);
        }

        return path;
    }

    @Override
    public Path persist(InboundMetadata inboundMetadata) throws IOException {
        return null;
    }
}
