package no.difi.oxalis.commons.io;

import java.io.IOException;
import java.io.InputStream;

public class UncloseableInputStream extends InputStream {

    private InputStream inputStream;

    public UncloseableInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        // No action.
    }
}
