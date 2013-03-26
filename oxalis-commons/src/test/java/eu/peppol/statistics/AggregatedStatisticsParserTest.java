package eu.peppol.statistics;

import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Collection;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 25.03.13
 *         Time: 14:58
 */
public class AggregatedStatisticsParserTest {

    @Test
    public void testParseSampleFile() throws Exception {

        InputStream sampleDataAsStream = getSampleDataAsStream();

        AggregatedStatisticsParser aggregatedStatisticsParser = new AggregatedStatisticsParser();
        Collection<AggregatedStatistics> statisticsCollection = aggregatedStatisticsParser.parse(getSampleDataAsStream());

        assertNotNull(statisticsCollection);
        assertFalse(statisticsCollection.isEmpty(), "No entities parsed");

    }

    InputStream getSampleDataAsStream() {
        return this.getClass().getClassLoader().getResourceAsStream("sample-stats-response.xml");
    }
}
