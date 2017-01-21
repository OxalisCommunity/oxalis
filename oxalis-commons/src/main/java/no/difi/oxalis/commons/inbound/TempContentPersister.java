package no.difi.oxalis.commons.inbound;

import com.google.common.io.ByteStreams;
import no.difi.oxalis.api.inbound.ContentPersister;
import no.difi.vefa.peppol.common.model.Header;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TempContentPersister implements ContentPersister {

    @Override
    public Path persist(Header header, InputStream inputStream) throws IOException {
        // Create temp file
        Path path = Files.createTempFile("oxalis-inbound", ".dat");

        // Copy content to temp file
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            ByteStreams.copy(inputStream, outputStream);
        }

        // Return file name
        return path;
    }
}
