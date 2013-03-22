package eu.peppol.statistics;

import eu.peppol.start.identifier.AccessPointIdentifier;
import eu.peppol.statistics.repository.DownloadRepository;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.testng.Assert.assertNotNull;

/**
 * User: steinar
 * Date: 07.03.13
 * Time: 15:56
 */
public class StatisticsDownloaderTest {

    public static final String ACCESS_POINTS_CSV = "access-points.csv";
    private File metaDataFile;
    private AccessPointMetaDataCollection accessPointMetaDataCollection;

    @BeforeTest
    public void setUp() throws URISyntaxException {
        URL url = StatisticsDownloaderTest.class.getClassLoader().getResource(ACCESS_POINTS_CSV);
        assertNotNull(url, "Unable to locate resource " + ACCESS_POINTS_CSV);

        metaDataFile = new File(url.toURI());

        accessPointMetaDataCollection = new AccessPointMetaDataCollection(metaDataFile);

    }

    @Test
    public void testDownload() throws URISyntaxException {

        setUp();

        String tmpDirName = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirName, "oxalis-statistics");

        StatisticsDownloader statisticsDownloader = new StatisticsDownloader(new DownloadRepository(tmpDir));


        URLRewriter addWsdlUrlRewriter = new URLRewriter() {
            // Assuming that the supplied URL points to the SOAP web service, we modify it to download the WSDL
            @Override
            public URL rewrite(URL url) {
                String s = url.toExternalForm() + "?wsdl";
                try {
                    return new URL(s);
                } catch (MalformedURLException e) {
                    throw new IllegalStateException("Invalid url after rewrite " + s);
                }
            }
        };

        statisticsDownloader.download(accessPointMetaDataCollection.getAccessPointMetaDataList(), addWsdlUrlRewriter);
        System.out.println("Data downloaded to " + tmpDir.getAbsolutePath());

    }

}
