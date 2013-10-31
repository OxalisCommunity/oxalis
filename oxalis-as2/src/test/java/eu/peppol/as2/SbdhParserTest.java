package eu.peppol.as2;

import eu.peppol.PeppolMessageMetaData;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.CustomizationIdentifier;
import eu.peppol.identifier.PeppolDocumentTypeId;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 25.10.13
 *         Time: 10:05
 */
public class SbdhParserTest {

    private SbdhParser sbdhParser;

    @BeforeMethod
    public void setUp() {
        sbdhParser = new SbdhParser();

    }
    @Test
    public void testParse() throws Exception {
        long start = System.currentTimeMillis();

        for (int i=0; i < 1000; i++){
            parseData();
        }

        long end = System.currentTimeMillis();

        long elapsed = end - start;

        System.out.println("Elapsed: " + elapsed);
    }


    private void parseData() throws URISyntaxException, IOException {
        URL resource = ParseSbdhTest.class.getClassLoader().getResource("peppol-bis-invoice-sbdh.xml");
        assertNotNull(resource);

        File file = new File(resource.toURI());
        assertTrue(file.isFile() && file.canRead());

// Test with really large file
//        File file2 = new File("/Users/steinar/Dropbox/SendRegning/bussinessdevelopment/Oxalis/AS2/sbdh/openPEPPOL Envelope SBDH_Super large.xml");


        FileInputStream fileInputStream = new FileInputStream(file);

        PeppolMessageMetaData info = sbdhParser.parse(fileInputStream);
        assertEquals(info.getRecipientId(), new ParticipantId("0007:4455454480"));
        assertEquals(info.getSenderId(), new ParticipantId("0007:5567125082"));
        assertEquals(info.getProfileTypeIdentifier(), "urn:www.cenbii.eu:profile:bii04:ver1.0");

        final PeppolDocumentTypeId invoice = new PeppolDocumentTypeId("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2", "Invoice", CustomizationIdentifier.valueOf("urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0"), "2.0");

        assertEquals(PeppolDocumentTypeId.valueOf(info.getDocumentTypeIdentifier()), invoice);

        assertEquals(info.getSendersTimeStamp().toString(), "Tue Feb 19 05:10:10 CET 2013");

        fileInputStream.close();
    }
}
