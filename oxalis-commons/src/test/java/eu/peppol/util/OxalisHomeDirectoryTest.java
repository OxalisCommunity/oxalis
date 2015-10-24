package eu.peppol.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;

import static org.testng.Assert.*;

/**
 * @author steinar
 * @author thore
 */
public class OxalisHomeDirectoryTest {

    @BeforeMethod
    public void clearSettingsForNextTest() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestableInitialContextFactory.class.getName());
        new InitialContext().unbind(OxalisHomeDirectory.OXALIS_HOME_JNDI_PATH);
    }

    @Test
    public void testFromJndi() throws Exception {

        String path = new File("/some/system/path").getAbsolutePath();
        File oxalis_home = null;

        //
        oxalis_home = new OxalisHomeDirectory().locateOxalisHomeFromLocalJndiContext();
        assertNull(oxalis_home);

        // bind value to JNDI and read
        new InitialContext().bind(OxalisHomeDirectory.OXALIS_HOME_JNDI_PATH, path);
        oxalis_home = new OxalisHomeDirectory().locateOxalisHomeFromLocalJndiContext();
        assertEquals(oxalis_home.getAbsolutePath(), path);

    }

    @Test
    public void testFromJavaSystemProperty() {

        String path = new File("/some/system/path").getAbsolutePath();
        String backup = System.getProperty(OxalisHomeDirectory.OXALIS_HOME_VAR_NAME);

        try {

            System.setProperty(OxalisHomeDirectory.OXALIS_HOME_VAR_NAME, "");
            File oxalis_home = new OxalisHomeDirectory().locateOxalisHomeFromJavaSystemProperty();
            assertNull(oxalis_home);

            System.setProperty(OxalisHomeDirectory.OXALIS_HOME_VAR_NAME, path);
            oxalis_home = new OxalisHomeDirectory().locateOxalisHomeFromJavaSystemProperty();
            assertEquals(oxalis_home.getAbsolutePath(), path);

        } finally {
            if (backup == null) backup = ""; // prevent null pointer exception
            System.setProperty(OxalisHomeDirectory.OXALIS_HOME_VAR_NAME, backup);
        }

    }

    @Test
    public void testFromEnvironmentVariable() {

        String path = System.getenv(OxalisHomeDirectory.OXALIS_HOME_VAR_NAME);
        File oxalis_home = new OxalisHomeDirectory().locateOxalisHomeFromEnvironmentVariable();

        // we cannot fake environment variables as they are in an UnmodifiableMap, only test when present
        if (path != null && path.length() > 0) {
            assertNotNull(oxalis_home);
            assertEquals(oxalis_home.getAbsolutePath(), path);
        } else {
            assertNull(oxalis_home);
        }

    }

    @Test
    public void testComputeOxalisHomeRelativeToUserHome() {

        String homeDirName = System.getProperty("user.home");
        File oxalisHomeDir = new File(homeDirName, ".oxalis");

        if (!oxalisHomeDir.exists())
            oxalisHomeDir.mkdir();

        File file = new OxalisHomeDirectory().computeOxalisHomeRelativeToUserHome();

        assertEquals(file, oxalisHomeDir);
    }

    @Test
    public void makeSureWeHaveWorkingOxalisHomeDirectory() {

        File file = new OxalisHomeDirectory().locateDirectory();
        assertTrue(file.exists(), "OXALIS_HOME was not found");
        assertTrue(file.isDirectory(), "OXALIS_HOME was not a directory");
        assertTrue(file.canRead(), "OXALIS_HOME was not readable");

    }

}
