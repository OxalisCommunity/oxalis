package eu.peppol.document;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 05.11.13
 *         Time: 15:14
 */
public class NoSbdh2PeppolHeaderParserTest {

    @Test
    public void sniffDocumentWithoutSBDH() throws Exception {

        InputStream resourceAsStream = NoSbdh2PeppolHeaderParserTest.class.getClassLoader().getResourceAsStream("ehf-invoice-no-sbdh.xml");
        assertNotNull(resourceAsStream);

        NoSbdhParser sniffer = new NoSbdhParser();
        PeppolStandardBusinessHeader sbdh = sniffer.parse(resourceAsStream);

        assertNotNull(sbdh.getDocumentTypeIdentifier());
        assertNotNull(sbdh.getCreationDateAndTime());
        assertNotNull(sbdh.getMessageId());
        assertNotNull(sbdh.getProfileTypeIdentifier());
        assertNotNull(sbdh.getRecipientId());
        assertNotNull(sbdh.getSenderId());

        assertEquals(sbdh.getSenderId(), new ParticipantId("9908:991974466"));
        assertEquals(sbdh.getRecipientId(), new ParticipantId("9908:889640782"));

        assertEquals(sbdh.getDocumentTypeIdentifier(), PeppolDocumentTypeIdAcronym.EHF_INVOICE.getDocumentTypeIdentifier());

    }

}
