package eu.peppol.statistics;

import eu.peppol.start.identifier.AccessPointIdentifier;
import eu.peppol.statistics.repository.DownloadRepository;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * User: steinar
 * Date: 07.03.13
 * Time: 15:56
 */
public class StatisticsDownloaderTest {

    public static final String ACCESS_POINTS_CSV = "access-points.csv";
    private File metaDataFile;
    private AccessPointMetaDataCollection accessPointMetaDataCollection;
    private StatisticsDownloader statisticsDownloader;

    @BeforeMethod
    public void setUp() throws URISyntaxException {
        URL url = StatisticsDownloaderTest.class.getClassLoader().getResource(ACCESS_POINTS_CSV);
        assertNotNull(url, "Unable to locate resource " + ACCESS_POINTS_CSV);

        metaDataFile = new File(url.toURI());

        accessPointMetaDataCollection = new AccessPointMetaDataCollection(metaDataFile);
        String tmpDirName = System.getProperty("java.io.tmpdir");

        File tmpDir = new File(tmpDirName, "oxalis-statistics");
        statisticsDownloader = new StatisticsDownloader(new DownloadRepository(tmpDir));

        System.out.println("Data downloaded to " + tmpDir.getAbsolutePath());
    }

    @Test(groups = {"integration"})
    public void testDownload() throws URISyntaxException {

        List<DownloadResult> downloadResults = statisticsDownloader.download(accessPointMetaDataCollection.getAccessPointMetaDataList());

        renderResults(downloadResults);
    }

    private void renderResults(List<DownloadResult> downloadResults) {
        for (DownloadResult downloadResult : downloadResults) {
            System.out.printf("%-20s %-130s %5dms %4d %s \n",
                    downloadResult.getAccessPointIdentifier(),
                    downloadResult.getDownloadUrl(),
                    downloadResult.getElapsedTimeInMillis(),
                    downloadResult.getHttpResultCode() != null ? downloadResult.getHttpResultCode() : -1,
                    downloadResult.getTaskFailureCause() == null ? "OK" : downloadResult.getTaskFailureCause().getMessage());
        }
    }

    @Test(groups = "integration")
    public void downloadFromLocalInstallation() throws Exception {

        AccessPointMetaData accessPointMetaData = new AccessPointMetaData(new AccessPointIdentifier("NO-SR"), "SendRegning", "976098897", "SendRegning local",
                new URL("https://localhost:8443/oxalis/accessPointService"),
                new URL("http://localhost:8080/oxalis/statistics"));

        ArrayList<AccessPointMetaData> accessPointMetaDataList = new ArrayList<AccessPointMetaData>();
        accessPointMetaDataList.add(accessPointMetaData);

        List<DownloadResult> downloadResults = statisticsDownloader.download(accessPointMetaDataList);
        renderResults(downloadResults);
    }

    @Test(groups = {"unit"})
    public void testComposeDownloadUrl() throws Exception {
        URL result = statisticsDownloader.composeDownloadUrl(new URL("http://aksesspunkt.sendregning.no/oxalis/statistics"), new DateTime("2013-01-01T00"), new DateTime());
        assertTrue(result.toExternalForm().matches("http://aksesspunkt.sendregning.no/oxalis/statistics\\?start=2013-01-01T00&end=\\d{4}-\\d{2}-\\d{2}T\\d{2}&granularity=H"), result.toExternalForm());
    }
}
