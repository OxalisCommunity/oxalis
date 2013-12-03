/* Created by steinar on 18.05.12 at 13:55 */
package eu.peppol.util;

import org.easymock.EasyMock;
import org.junit.Ignore;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import sun.misc.BASE64Encoder;

import javax.mail.util.SharedByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class UtilTest {

    @Test
    public void testCalculateMD5() throws Exception {
        String hash = Util.calculateMD5("9908:810017902");

        assertEquals(hash, "ddc207601e442e1b751e5655d39371cd");
    }


    /**
     * Experiments with byte arrays in order to verify that our understanding of the API is correct
     */
    @Test
    public void duplicateInputStream() throws Exception {

        InputStream inputStream = UtilTest.class.getClassLoader().getResourceAsStream("peppol-bis-invoice-sbdh.xml");
        assertNotNull(inputStream);

        byte[] bytes = Util.intoBuffer(inputStream, 5L * 1024 * 1024);
        String s = new String(bytes);
        assertTrue(s.contains("</StandardBusinessDocument>"));


        SharedByteArrayInputStream sharedByteArrayInputStream = new SharedByteArrayInputStream(bytes);
        InputStream inputStream1 = sharedByteArrayInputStream.newStream(0, -1);

        byte[] b2 = Util.intoBuffer(inputStream1, 5L * 1024 * 1024);
        assertEquals(bytes.length, b2.length);
    }

    @Test(expectedExceptions = ConnectionException.class)
    public void test404() throws Exception {
        Util.getUrlContent(new URL("http://smp.difi.no/iso6523-actorid-upis%3A%3A9908%3A9854323/"));
    }

    @Test(enabled = false, expectedExceptions = TryAgainLaterException.class)
    public void test503() throws Exception {
        URL mock = EasyMock.createMock(URL.class);
        HttpURLConnection httpURLConnection = EasyMock.createMock(HttpURLConnection.class);
        EasyMock.expect(mock.openConnection()).andReturn(httpURLConnection);
        httpURLConnection.connect();
        EasyMock.expect(httpURLConnection.getContentEncoding()).andReturn("");
        EasyMock.expect(httpURLConnection.getResponseCode()).andReturn(503);
        EasyMock.expect(httpURLConnection.getHeaderField("Retry-After")).andReturn("120");

        EasyMock.replay(mock, httpURLConnection);

        Util.getUrlContent(mock);
    }
}
