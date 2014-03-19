package eu.peppol.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 10.04.13
 *         Time: 10:50
 */
public class OxalisVersionTest {

    @Test
    public void testGetVersion() throws Exception {
        String currentVersion = OxalisVersion.getVersion();
        assertNotNull(currentVersion);
        System.out.printf("Current version is %s\n", currentVersion);
    }

    public void testGetBuildDescription() throws Exception {
        String buildDescription = OxalisVersion.getBuildDescription();
        assertNotNull(buildDescription);
        System.out.printf("Description is %s\n", buildDescription);
    }

}
