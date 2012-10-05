package eu.peppol.inbound.server;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.sun.xml.ws.transport.http.HttpAdapter;
import eu.peppol.inbound.util.Log;
import eu.peppol.inbound.util.LoggingConfigurator;
import eu.peppol.start.identifier.Configuration;
import eu.peppol.start.identifier.KeystoreManager;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.net.URL;

/**
 * User: nigel and steinar
 * Date: Oct 24, 2011
 * Time: 3:08:30 PM
 */
@SuppressWarnings({"AccessStaticViaInstance"})
public class ContextListener implements ServletContextListener {

    SimpleLogger simpleLocalLogger = null;

    public void contextInitialized(ServletContextEvent event) {

        simpleLocalLogger = new SimpleLoggerImpl(event.getServletContext());

        initializeLogging(event);

        Log.info("Starting Oxalis Access Point");
        Log.debug("Initialising keystore");

        try {
            KeystoreManager keystoreManager = new KeystoreManager();
            Configuration configuration = Configuration.getInstance();

            File keystore = locateKeystore(configuration);

            String keystorePassword = configuration.getKeyStorePassword();
            keystoreManager.initialiseKeystore(keystore, keystorePassword);
            Log.debug("Keystore initialised from " + keystore);

            if (configuration.isSoapTraceEnabled()) {
                HttpAdapter.dump = true;
            }
        } catch (RuntimeException e) {
            Log.error("Unable to initialize: " + e, e);

            // Shoves a decent error message into the Tomcat log
            event.getServletContext().log("ERROR: Unable to initialize: " + e, e);
            throw e;
        }
    }

    protected void initializeLogging(ServletContextEvent event) {
        simpleLocalLogger.log("Oxalis messages are emitted using SLF4J with logback, see logback-oxalis.xml");
        try {
            LoggingConfigurator loggingConfigurator = new LoggingConfigurator("logback-oxalis.xml");
            loggingConfigurator.execute();
            simpleLocalLogger.log("Configured logback with " + loggingConfigurator.getConfigurationFile());
        } catch (Exception e) {
            simpleLocalLogger.log("Failed to configure logging");
        }
    }


    File locateKeystore(Configuration configuration) {
        String keystoreFileName = configuration.getKeyStoreFileName();
        File keystoreFile = new File(keystoreFileName);
        if (!keystoreFile.exists()) {
            throw new IllegalStateException("Keystore file does not exist:" + keystoreFileName);
        }
        if (!keystoreFile.canRead()) {
            throw new IllegalStateException("Unable to read keystore in:" + keystoreFileName + ", check permissions");
        }
        return keystoreFile;
    }

    public void contextDestroyed(ServletContextEvent event) {
        Log.info("Stopping Oxalis Access Point");
    }

    static interface SimpleLogger {
        void log(String msg);
    }

    static class SimpleLoggerImpl implements SimpleLogger {

        ServletContext servletContext;

        SimpleLoggerImpl(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        public void log(String msg) {
            servletContext.log(msg);
        }
    }
}

