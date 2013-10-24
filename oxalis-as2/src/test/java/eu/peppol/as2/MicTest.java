package eu.peppol.as2;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 22.10.13
 *         Time: 16:01
 */
public class MicTest {
    @Test
    public void testToString() throws Exception {
        Mic mic = new Mic("eeWNkOTx7yJYr2EW8CR85I7QJQY=", "sha1");
        assertNotNull(mic);
    }

    @Test
    public void testValueOf() throws Exception {

        Mic mic = Mic.valueOf("eeWNkOTx7yJYr2EW8CR85I7QJQY=, sha1");
        assertNotNull(mic);
    }
}
