package no.difi.oxalis.commons.io;

import java.io.*;

/**
 * @author erlend
 * @since 4.0.0
 */
public class PeekingInputStream extends InputStream {

    private InputStream sourceInputStream;

    private ByteArrayOutputStream cacheOutputStream = new ByteArrayOutputStream();

    public PeekingInputStream(InputStream sourceInputStream) {
        this.sourceInputStream = sourceInputStream;
    }

    @Override
    public int read() throws IOException {
        // Read byte
        int b = sourceInputStream.read();

        // Write to internal stream
        cacheOutputStream.write(b);

        // Return byte
        return b;
    }

    public InputStream newInputStream() throws IOException {
        return new SequenceInputStream(
                new ByteArrayInputStream(cacheOutputStream.toByteArray()),
                sourceInputStream
        );
    }
}
