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
        lc.execute();   // Should throw an exception since there should not be a logback.xml provided
    }

    @Test
    public void configureLoggingFromFileInClasspath() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        System.setOut(ps);

        // Configures the logging using the configuration file we specify
        LoggingConfigurator lc = new LoggingConfigurator("logback-test2.xml");
        lc.execute();

        assertFalse(baos.toString().contains("ERROR"), "There are errors in the logback-test2.xml file");
    }


    @Test
    public void configureLoggingFromFileWithErrorsInClasspath() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        System.setOut(ps);

        // Configures the logging using the configuration file we specify
        LoggingConfigurator lc = new LoggingConfigurator("logback-error.xml");
        lc.execute();

        ps.flush();
        String output = baos.toString();

        // TODO: Figure out why the output string is empty

    }

    @Test(expectedExceptionsMessageRegExp = "Unable to locate")
    public void configureWithNonExistentFile() {
        LoggingConfigurator lc = new LoggingConfigurator("non-existent-file.xml");
        lc.execute();
    }

}
