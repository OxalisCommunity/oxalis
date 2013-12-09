package eu.peppol.inbound.as2;

import eu.peppol.as2.As2Header;
import eu.peppol.as2.As2Message;
import static org.easymock.EasyMock.*;

import eu.peppol.as2.As2MessageFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 07.10.13
 *         Time: 21:57
 */
public class As2MessageFactoryTest {


    private ServletInputStream mockServletInputStream;

    /**
     * Wraps an InputStream in a ServletInputStream as this is what is required
     * to be returned by HttpServletRequest#getInputStream()
     */
    static class MockServletInputStream extends ServletInputStream {

        private final InputStream inputStream;

        MockServletInputStream(InputStream inputStream) {

            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }

    /**
     * Wraps the test resource signed2.message in a ServletInputStream to provide sample data.
     */
    @BeforeMethod
    public void createInputStream() {
        InputStream resourceAsStream = As2MessageFactoryTest.class.getClassLoader().getResourceAsStream("signed2.message");
        assertNotNull(resourceAsStream);
    }

}
