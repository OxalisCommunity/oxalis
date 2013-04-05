package eu.peppol.statistics;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Collection;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 25.03.13
 *         Time: 14:58
 */
public class AggregatedStatisticsParserTest {


    private AggregatedStatisticsParser aggregatedStatisticsParser;

    @BeforeTest
    public void setup() {
        aggregatedStatisticsParser = new AggregatedStatisticsParser();
    }


    @Test
    public void testParseSampleFile() throws Exception {


        InputStream sampleDataAsStream = getSampleDataAsStream("sample-stats-response.xml");
        Collection<AggregatedStatistics> statisticsCollection = aggregatedStatisticsParser.parse(sampleDataAsStream);

        assertNotNull(statisticsCollection);
        assertFalse(statisticsCollection.isEmpty(), "No entities parsed");

    }


    @Test
    public void parseSampleFileWithMultipleEntries() throws Exception {
        InputStream is = getSampleDataAsStream("statistics-response-multiple-entries.xml");

        Collection<AggregatedStatistics> statisticsCollection = aggregatedStatisticsParser.parse(is);

        assertEquals(statisticsCollection.size(), 15);
    }


    InputStream getSampleDataAsStream(String resourceName) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(resourceName);
        if (resourceAsStream == null) {
            throw new IllegalStateException("Unable to find resource " + resourceName + " in class path");
        }
        return resourceAsStream;
    }
}
