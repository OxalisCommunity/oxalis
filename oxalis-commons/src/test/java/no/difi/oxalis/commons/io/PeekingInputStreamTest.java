package no.difi.oxalis.commons.io;

import com.google.common.io.ByteStreams;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class PeekingInputStreamTest {

    @Test
    public void simple() throws IOException {
        PeekingInputStream peekingInputStream = new PeekingInputStream(
                new ByteArrayInputStream("Hello World!".getBytes()));

        byte[] bytes1 = new byte[5];
        peekingInputStream.read(bytes1);
        Assert.assertEquals(new String(bytes1), "Hello");

        Assert.assertEquals(new String(ByteStreams.toByteArray(peekingInputStream.newInputStream())), "Hello World!");
    }
}