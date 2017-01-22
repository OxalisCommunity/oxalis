package no.difi.oxalis.commons.io;

import com.google.common.io.ByteStreams;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UnclosableInputStreamTest {

    @Test
    public void simple() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("Hello World!".getBytes());

        try (InputStream content = new UnclosableInputStream(inputStream)) {
            ByteStreams.exhaust(content);
        } // close() is called here.

        Assert.assertEquals(inputStream.read(), -1);
    }
}
