package eu.peppol.statistics.repository;

import org.testng.annotations.Test;

import javax.activation.MimeType;

import static org.testng.Assert.assertEquals;


/**
 * @author steinar
 *         Date: 22.03.13
 *         Time: 10:02
 */
public class MimeTypeTest {
    @Test
    public void testMimeType() throws Exception {

        MimeType mimeType = new MimeType("text/xml");
        assertEquals(mimeType.getPrimaryType(), "text");
        assertEquals(mimeType.getSubType(), "xml");

    }
}
