package eu.peppol.inbound.as2;

import eu.peppol.as2.As2Header;
import eu.peppol.as2.As2Message;
import static org.easymock.EasyMock.*;
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
        mockServletInputStream = new MockServletInputStream(resourceAsStream);
    }

    @Test
    public void createAsMessageFromMockServletRequest() throws Exception {

        // Records the invocations performed on the Mock Servlet
        HttpServletRequest mockServletRequest = createMock(HttpServletRequest.class);
        expect(mockServletRequest.getInputStream()).andReturn(mockServletInputStream);
        expect(mockServletRequest.getHeader(As2Header.AS2_VERSION.getHttpHeaderName())).andReturn("1.1");
        expect(mockServletRequest.getHeader(As2Header.AS2_FROM.getHttpHeaderName())).andReturn("APP_1000000001");
        expect(mockServletRequest.getHeader(As2Header.AS2_TO.getHttpHeaderName())).andReturn("APP_100000999");
        expect(mockServletRequest.getHeader(As2Header.SUBJECT.getHttpHeaderName())).andReturn("This is a PEPPOL message");
        expect(mockServletRequest.getHeader(As2Header.DATE.getHttpHeaderName())).andReturn("Mon Oct  7 23:08:54 CEST 2013");
        expect(mockServletRequest.getHeader(As2Header.MESSAGE_ID.getHttpHeaderName())).andReturn("42");

        // Not really used for anything, might as well just leave it out for now
        // expect(mockServletRequest.getHeader(As2Header.DISPOSITION_NOTIFICATION_TO.getHttpHeaderName())).andReturn("steinar@sendregning.no");

        expect(mockServletRequest.getHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName())).andReturn("signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        expect(mockServletRequest.getHeader(As2Header.RECEIPT_DELIVERY_OPTION.getHttpHeaderName())).andReturn("ap.unit4.com/oxalis/as2/receipt/42");

        replay(mockServletRequest); // Done, makes the mock object ready for replay

        // Performs the actual test
        As2Message as2Message = As2MessageFactory.createAs2MessageFrom(mockServletRequest);
        // Verifies that all properties have been set as expected.
        assertNotNull(as2Message.getMimeMessage());
        assertNotNull(as2Message.getAs2From());
        assertNotNull(as2Message.getAs2To());
        assertNotNull(as2Message.getAs2Version());
        assertNotNull(as2Message.getSubject());
        assertNotNull(as2Message.getDate());
        assertNotNull(as2Message.getMessageId());
        assertNotNull(as2Message.getDispositionNotificationOptions());
        assertNotNull(as2Message.getReceiptDeliveryOption());

        // Verifies that all expected method calls have been made on the Mock servlet request
        verify(mockServletRequest);
    }
}
