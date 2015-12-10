package eu.peppol.inbound.util;

import com.google.inject.Inject;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.RuntimeConfigurationModule;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.File;
import java.io.PrintStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: steinar
 * Date: 04.10.12
 * Time: 13:42
 */
@Test(groups = "integration")
@Guice(modules = {RuntimeConfigurationModule.class})
public class LoggingConfiguratorTest {

    public static final String FILE_NAME = "logback-test.xml";
    private PrintStream out;

    @Inject
    GlobalConfiguration globalConfiguration;

    @BeforeMethod
    public void redirectStdoutAndStderr() {
        out = System.out;
    }

    @AfterTest
    public void restoreOutputStreams(){
        System.setOut(out);
    }

    @Test
    public void locateConfigurationFileInClassPath() {
        LoggingConfigurator loggingConfigurator = new LoggingConfigurator(globalConfiguration);

        File logConfigFile = loggingConfigurator.locateLoggingConfigurationFileInClassPathBySimpleName(FILE_NAME);
        assertNotNull(logConfigFile,FILE_NAME + " not located in class path,");
        assertEquals(logConfigFile.getName(), FILE_NAME);
    }

    @Test
    public void comfigureLoggingUsingDefaultConfigFile() {
        LoggingConfigurator lc = new LoggingConfigurator(globalConfiguration);
        lc.execute();
    }
}
