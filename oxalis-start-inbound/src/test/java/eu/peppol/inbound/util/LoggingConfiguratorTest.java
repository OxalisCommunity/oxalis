package eu.peppol.inbound.util;

import org.testng.annotations.*;

import java.io.*;

import static org.testng.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: steinar
 * Date: 04.10.12
 * Time: 13:42
 */
@Test(groups = "integration")
public class LoggingConfiguratorTest {

    public static final String FILE_NAME = "logback-test.xml";
    private PrintStream out;

    @BeforeTest
    public void redirectStdoutAndStderr() {
        out = System.out;
    }

    @AfterTest
    public void restoreOutputStreams(){
        System.setOut(out);
    }

    @Test
    public void locateConfigurationFileInClassPath() {
        LoggingConfigurator loggingConfigurator = new LoggingConfigurator();

        File logConfigFile = loggingConfigurator.locateLoggingConfigurationFileInClassPathBySimpleName(FILE_NAME);
        assertNotNull(logConfigFile,FILE_NAME + " not located in class path,");
        assertEquals(logConfigFile.getName(), FILE_NAME);
    }

    @Test
    public void comfigureLoggingUsingDefaultConfigFile() {
        LoggingConfigurator lc = new LoggingConfigurator();
        lc.execute();
    }
}
