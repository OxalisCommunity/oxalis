package eu.peppol.inbound.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Configures the SLF4J logging configuration.
 * <p/>
 * User: steinar
 * Date: 04.10.12
 * Time: 13:43
 */
public class LoggingConfigurator {

    // First file we are looking for, this may be modified when creating objects of this class
    private String currentSimpleConfigFileName = "logback-oxalis.xml";

    // If locating the above file name fails, fall back to this file name
    private static String defaultSimpleConfigFilename = "logback.xml";

    private File configFile = null;

    public LoggingConfigurator(String defaultSimpleFilename) {
        if (defaultSimpleFilename == null) {
            throw new IllegalArgumentException("Default simple logging configuration filename required");
        }
        this.currentSimpleConfigFileName = defaultSimpleFilename;
    }

    public LoggingConfigurator() {
    }

    File locateLoggingConfigurationFileInClassPathBySimpleName(String fileName) {
        URL url = LoggingConfigurator.class.getClassLoader().getResource(fileName);
        if (url != null) {
            try {
                return new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Unable to convert " + url + " into URI for File object");
            }
        } else {
            return null;
        }
    }

    File locateConfigFile() {
        File f = locateLoggingConfigurationFileInClassPathBySimpleName(currentSimpleConfigFileName);
        if (f == null) {
            if (!defaultSimpleConfigFilename.equals(currentSimpleConfigFileName)) {
                f = locateLoggingConfigurationFileInClassPathBySimpleName(defaultSimpleConfigFilename);
                if (f == null) {
                    throw new IllegalStateException("Unable to locate either " + currentSimpleConfigFileName + " or " + defaultSimpleConfigFilename + " in classpath");
                }
            } else {
                throw new IllegalStateException("Unable to locate " + currentSimpleConfigFileName + " in classpath");
            }
        }
        return f;
    }


    public void execute() {

        configFile = locateConfigFile();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        // Call context.reset() to clear any previous configuration, e.g. default
        // configuration. For multi-step configuration, omit calling context.reset().
        loggerContext.reset();
        try {
            configurator.doConfigure(configFile);
            StatusPrinter.print(loggerContext);
        } catch (JoranException e) {
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
    }

    public File getConfigurationFile() {
        return configFile;
    }
}
