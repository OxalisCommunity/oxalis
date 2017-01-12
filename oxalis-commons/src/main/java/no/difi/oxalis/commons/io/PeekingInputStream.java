package no.difi.oxalis.commons.io;

import java.io.*;

public class PeekingInputStream extends InputStream {

    private InputStream inputStream;

    private ByteArrayOutputStream cacheOutputStream = new ByteArrayOutputStream();

    public PeekingInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        // Read byte
        int b = inputStream.read();

        // Write to internal stream
        cacheOutputStream.write(b);

        // Return byte
        return b;
    }

    public InputStream newInputStream() throws IOException {
        return new SequenceInputStream(
                new ByteArrayInputStream(cacheOutputStream.toByteArray()),
                inputStream
        );
    }
}
