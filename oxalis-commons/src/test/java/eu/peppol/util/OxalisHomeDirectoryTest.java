package eu.peppol.util;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

/**
 * User: steinar
 * Date: 08.02.13
 * Time: 10:22
 */
public class OxalisHomeDirectoryTest {

    @Test
    public void testComputeOxalisHomeRelativeToUserHome() {
        File file = new OxalisHomeDirectory().computeOxalisHomeRelativeToUserHome();
        String homeDirName = System.getProperty("user.home");
        File oxalisHomeDir = new File(homeDirName, ".oxalis");
        assertEquals(oxalisHomeDir, file);
    }


    @Test
    public void testLocateOxalisHomeDirRelativeToUserHome() throws Exception {

        File computedHome = new OxalisHomeDirectory().computeOxalisHomeRelativeToUserHome();

        try {
            File file = new OxalisHomeDirectory().locateOxalisHomeDirRelativeToUserHome();
        } catch (IllegalStateException e) {
            if (computedHome.exists()) {
                fail("Oxalis home relative to user home exists, but fails!");
            }
        }
    }

    @Test
    public void testLocateOxalisHomeFromEnvironmentVariable() {
        File file = new OxalisHomeDirectory().locateOxalisHomeFromEnvironmentVariable();
        if (System.getenv(OxalisHomeDirectory.OXALIS_HOME_VAR_NAME) == null) {
            assertNull(file);
        } else {
            assertNotNull(file);
        }
    }

    @Test
    public void testOxalisHomeDirectory() throws Exception {

    }


}
