package eu.peppol.statistics;

import eu.peppol.statistics.repository.DownloadRepository;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author steinar
 *         Date: 22.03.13
 *         Time: 17:56
 */
public class StatisticsImporterTest {


    private File downloadRepoDir;

    @BeforeTest
    public void setUp() {
        downloadRepoDir = new File(System.getProperty("java.io.tmpdir"), "oxalis-test");
        // Prepare some data
    }

    @Test
    public void testLoadSaveAndArchive() throws Exception {

        StatisticsImporter statisticsImporter = new StatisticsImporter(new DownloadRepository(downloadRepoDir));
        statisticsImporter.loadSaveAndArchive();
    }
}
