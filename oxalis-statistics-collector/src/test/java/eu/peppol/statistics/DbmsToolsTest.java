package eu.peppol.statistics;

import eu.peppol.util.GlobalConfiguration;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author steinar
 *         Date: 03.04.13
 *         Time: 21:40
 */
@Test(groups = {"integration"})
public class DbmsToolsTest {

    private GlobalConfiguration globalConfiguration;

    @BeforeTest
    public void setUp() {
        globalConfiguration = GlobalConfiguration.getInstance();

    }

    @Test
    public void testCreateDatabaseSchema() throws Exception {

        DbmsTools dbmsTools = new DbmsTools();

        dbmsTools.createDatabaseSchema();
    }

}
