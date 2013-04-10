package eu.peppol.statistics;

import eu.peppol.start.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.start.identifier.PeppolProcessTypeIdAcronym;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import static org.testng.Assert.assertTrue;

/**
 * User: steinar
 * Date: 24.02.13
 * Time: 10:46
 */
public class StatisticsToXmlTransformerTest {

    private StatisticsToXmlTransformer transformer;
    private ByteArrayOutputStream byteArrayOutputStream;

    @BeforeMethod
    public void setUp() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        transformer = new StatisticsToXmlTransformer(byteArrayOutputStream);
    }

    @Test
    public void testWriteSampleStatisticsToXml() throws UnsupportedEncodingException {
        Date now = new Date();
        Date start = new Date(now.getTime()-86400000L);
        Date end = new Date();

        transformer.startStatistics(start, end);
        transformer.startEntry();
        transformer.writeAccessPointIdentifier("AP-0001");
        transformer.writeParticipantIdentifier("9908:810017902");
        transformer.writeDirection(Direction.OUT.name());
        transformer.writePeriod("2013-01-T13");
        transformer.writeDocumentType(PeppolDocumentTypeIdAcronym.INVOICE.toString());
        transformer.writeProfileId(PeppolProcessTypeIdAcronym.INVOICE_ONLY.toString());
        transformer.writeChannel("SR-TEST");
        transformer.writeCount(10);
        transformer.endEntry();
        transformer.endStatistics();

        String s = byteArrayOutputStream.toString("UTF-8");

        assertTrue(s.contains(StatisticsTransformer.STATISTICS_DOCUMENT_START_ELEMENT_NAME));
        assertTrue(s.contains(StatisticsTransformer.ENTRY_START_ELEMENT_NAME));
        assertTrue(s.contains(StatisticsTransformer.ACCESS_POINT_ID_ELEMENT_NAME), "Missing " + StatisticsTransformer.PARTICIPANT_ID_ELEMENT_NAME + " element in result");
        assertTrue(s.contains(StatisticsTransformer.PARTICIPANT_ID_ELEMENT_NAME), "Missing " + StatisticsTransformer.PARTICIPANT_ID_ELEMENT_NAME);
        assertTrue(s.contains(StatisticsTransformer.CHANNEL_ELEMENT_NAME), "Missing " + StatisticsTransformer.CHANNEL_ELEMENT_NAME);
        assertTrue(s.contains(StatisticsTransformer.DOCUMENT_TYPE_ELEMENT_NAME));
        assertTrue(s.contains(StatisticsTransformer.PROFILE_ID_ELEMENT_NAME));
        assertTrue(s.contains(StatisticsTransformer.COUNT_ELEMENT_NAME));

        System.err.println(s);
    }

}
