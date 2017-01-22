package no.difi.oxalis.commons.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple wrapper of an InputStream making sure the close method on the encapsulated InputStream is never called.
 *
 * @author erlend
 * @since 4.0.0
 */
public class UnclosableInputStream extends InputStream {

    private InputStream inputStream;

    public UnclosableInputStream(InputStream inputStream) {
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
