package eu.peppol.statistics;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 05.04.13
 *         Time: 16:23
 */
class TestUtil {


    static AccessPointMetaDataCollection loadSampleAccessPointMetaData() {


        URL url = AccessPointMetaDataCollectionTest.class.getClassLoader().getResource("access-points.csv");
        assertNotNull(url, "Unable to locate file holding access point meta data");
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to convert " + url.toExternalForm() + " into a URI", e);
        }
        AccessPointMetaDataCollection accessPointMetaDataCollection = new AccessPointMetaDataCollection(file);
        return accessPointMetaDataCollection;
    }

}
