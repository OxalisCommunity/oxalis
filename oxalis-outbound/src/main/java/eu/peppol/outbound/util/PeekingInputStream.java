package eu.peppol.outbound.util;

import java.io.*;

public class PeekingInputStream extends InputStream {

    private InputStream inputStream;

    private ByteArrayOutputStream cacheOutputStream;

    private boolean performCaching;

    public PeekingInputStream(InputStream inputStream) {
        this.inputStream = inputStream;

        this.performCaching = !inputStream.markSupported();
        if (performCaching)
            cacheOutputStream = new ByteArrayOutputStream();
    }

    @Override
    public int read() throws IOException {
        // Read byte
        int b = inputStream.read();

        // Write to internal stream
        if (performCaching)
            cacheOutputStream.write(b);

        // Return byte
        return b;
    }

    public InputStream newInputStream() throws IOException {
        if (performCaching)
            return new SequenceInputStream(
                    new ByteArrayInputStream(cacheOutputStream.toByteArray()),
                    inputStream
            );
        else {
            inputStream.reset();
            return inputStream;
        }
    }
}
