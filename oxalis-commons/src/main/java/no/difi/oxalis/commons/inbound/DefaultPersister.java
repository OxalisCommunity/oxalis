package no.difi.oxalis.commons.inbound;

import com.google.common.io.ByteStreams;
import no.difi.oxalis.api.inbound.ContentPersister;
import no.difi.vefa.peppol.common.model.Header;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultPersister implements ContentPersister {

    @Override
    public Path persist(Header header, InputStream inputStream) throws IOException {
        Path path = Paths.get(
                "inbound",
                header.getReceiver().getIdentifier(),
                header.getSender().getIdentifier(),
                String.format("%s.xml", header.getIdentifier().getValue())
        );

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            ByteStreams.copy(inputStream, outputStream);
        }

        return path;
    }
}
