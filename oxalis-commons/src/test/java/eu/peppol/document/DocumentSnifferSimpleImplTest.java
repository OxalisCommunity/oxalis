package eu.peppol.document;

import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 06.11.13
 *         Time: 16:13
 */
public class DocumentSnifferSimpleImplTest {

    @Test
    public void sniffDocument() throws Exception {

        InputStream resourceAsStream = NoSbdh2PeppolHeaderParserTest.class.getClassLoader().getResourceAsStream("ehf-invoice-no-sbdh.xml");
        assertNotNull(resourceAsStream);

        if (resourceAsStream.markSupported()) {
            resourceAsStream.mark(Integer.MAX_VALUE);
        }

        DocumentSniffer documentSniffer = new DocumentSnifferSimpleImpl(resourceAsStream);
        assertFalse(documentSniffer.isSbdhDetected());
        resourceAsStream.reset();
        resourceAsStream.close();

        InputStream sbdhStream = NoSbdh2PeppolHeaderParserTest.class.getClassLoader().getResourceAsStream("peppol-bis-invoice-sbdh.xml");
        assertNotNull(sbdhStream);
        documentSniffer = new DocumentSnifferSimpleImpl(sbdhStream);
        assertTrue(documentSniffer.isSbdhDetected());
        assertTrue(sbdhStream.markSupported());

    }

}
