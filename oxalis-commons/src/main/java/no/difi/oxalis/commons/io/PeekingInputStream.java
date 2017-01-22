package no.difi.oxalis.commons.io;

import java.io.*;

/**
 * Caching InputStream to be used when reading the beginning of a stream is needed before the stream is "reset" when
 * the exact amount of data is unknown and support for marking of is irrelevant.
 *
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
