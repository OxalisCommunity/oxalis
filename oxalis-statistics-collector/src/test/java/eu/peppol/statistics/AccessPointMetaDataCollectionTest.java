package eu.peppol.statistics;

import eu.peppol.identifier.AccessPointIdentifier;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author steinar
 * Date: 07.03.13
 * Time: 14:11
 */
public class AccessPointMetaDataCollectionTest {

    private AccessPointMetaDataCollection accessPointMetaDataCollection;

    @BeforeMethod
    public void setUp() throws Exception {
        accessPointMetaDataCollection = AccessPointMetaDataCollection.loadIncludedListOfAccessPoints();
    }


    @Test
    public void testCreate() {
        List<AccessPointMetaData> metaDataList = accessPointMetaDataCollection.getAccessPointMetaDataList();

        for (AccessPointMetaData data : metaDataList) {
            assertNotNull(data.getAccessPointIdentifier(), "Access point identifier is missing " + data);
            assertNotNull(data.getCompanyName(), "CompanyName missing " + data);
            assertNotNull(data.getDescription(), "Description of access point missing " + data);
            assertNotNull(data.getOrgNo(),"OrgNo missing " + data);
            assertNotNull(data.getAccessPointServiceUrl(), "URL missing " + data);

            // Currently (March 31, 2013) there is only a statistics URL at sendregning.no
            if (data.getAccessPointIdentifier().equals(new AccessPointIdentifier("NO-SR")) == false) {
                assertNull(data.getStatisticsUrl(), "Did not expect statistics URL for " + data.getAccessPointIdentifier());
            }
        }
    }
}
