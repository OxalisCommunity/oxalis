package eu.peppol.statistics;

import eu.peppol.statistics.repository.DownloadRepository;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 22.03.13
 *         Time: 17:56
 */
@Test(groups = {"integration"})
public class StatisticsImporterTest {


    public static final int COUNT_OF_SAMPLE_ENTRIES = 10;
    private File downloadRepoDir;
    private DownloadRepository downloadRepository;
    private AggregatedStatisticsRepository aggregatedStatisticsRepository;
    private Collection<AggregatedStatistics> aggregatedStatistics;

    @BeforeTest
    public void setUp() {

        // Establishes our file based download repository
        downloadRepoDir = new File(System.getProperty("java.io.tmpdir"), "oxalis-test");
        downloadRepository = new DownloadRepository(downloadRepoDir);

        AggregatedStatisticsSampleGenerator aggregatedStatisticsSampleGenerator = new AggregatedStatisticsSampleGenerator();

        // Prepares some data
        aggregatedStatistics = aggregatedStatisticsSampleGenerator.generateEntries(COUNT_OF_SAMPLE_ENTRIES);
        assertNotNull(aggregatedStatistics);

        assertEquals(aggregatedStatistics.size(), COUNT_OF_SAMPLE_ENTRIES);

        // Creates an instance of our DBMS statistics repository
        aggregatedStatisticsRepository = StatisticsRepositoryFactoryProvider.getInstance().getInstanceForAggregatedStatistics();
    }

    @Test
    public void testInsertEntriesInDatabase() {
        int i=0;
        for (AggregatedStatistics statisticsEntry : aggregatedStatistics) {
            i++;
            aggregatedStatisticsRepository.persist(statisticsEntry);
        }
    }

    @Test
    public void testLoadSaveAndArchive() throws Exception {

        StatisticsImporter statisticsImporter = new StatisticsImporter(downloadRepository);
        statisticsImporter.loadSaveAndArchive();
    }
}
