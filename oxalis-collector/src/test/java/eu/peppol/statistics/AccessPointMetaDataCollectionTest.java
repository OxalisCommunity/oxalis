package eu.peppol.statistics;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 * Date: 07.03.13
 * Time: 14:11
 */
public class AccessPointMetaDataCollectionTest {

    private File file;

    @BeforeMethod
    public void setUp() throws Exception {

        URL url = AccessPointMetaDataCollectionTest.class.getClassLoader().getResource("access-points.csv");
        assertNotNull(url, "Unable to locate file holding access point meta data");
        file = new File(url.toURI());
    }

    @Test
    public void testCreate() {
        AccessPointMetaDataCollection accessPointMetaDataCollection = new AccessPointMetaDataCollection(file);
        List<AccessPointMetaData> metaDataList = accessPointMetaDataCollection.getAccessPointMetaDataList();

        for (AccessPointMetaData data : metaDataList) {
            assertNotNull(data.getAccessPointIdentifier(), "Access point identifier is missing " + data);
            assertNotNull(data.getCompanyName(), "CompanyName missing " + data);
            assertNotNull(data.getDescription(), "Description of access point missing " + data);
            assertNotNull(data.getOrgNo(),"OrgNo missing " + data);
            assertNotNull(data.getUrl(), "URL missing " + data);
        }
    }
}
