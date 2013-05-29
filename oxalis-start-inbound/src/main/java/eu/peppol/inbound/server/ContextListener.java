package eu.peppol.inbound.server;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.sun.xml.ws.transport.http.HttpAdapter;
import eu.peppol.inbound.util.Log;
import eu.peppol.inbound.util.LoggingConfigurator;
import eu.peppol.start.identifier.KeystoreManager;
import eu.peppol.util.GlobalConfiguration;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

/**
 * This ContextListener serves as the main runtime initialization point for Oxalis.
 *
 * User: nigel and steinar
 * Date: Oct 24, 2011
 * Time: 3:08:30 PM
 */
@SuppressWarnings({"AccessStaticViaInstance"})
public class ContextListener implements ServletContextListener {

    SimpleLogger simpleLocalLogger = null;

    public ContextListener() {
        System.err.println("Initializing the Oxalis inbound server ....");
    }

    public void contextInitialized(ServletContextEvent event) {

        simpleLocalLogger = new SimpleLoggerImpl(event.getServletContext());
        System.out.println("PEPPOL Context listener starting ...");

        initializeLogging(event);

        Log.info("Starting Oxalis Access Point");
        Log.debug("Initialising keystore");

        try {
            GlobalConfiguration globalConfiguration = GlobalConfiguration.getInstance();

            if (globalConfiguration.isSoapTraceEnabled()) {
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
        System.err.println("Oxalis messages are emitted using SLF4J with logback");
        try {
            // Invokes the Oxalis logging configurator
            LoggingConfigurator loggingConfigurator = new LoggingConfigurator();
            loggingConfigurator.execute();

            simpleLocalLogger.log("Configured logback with " + loggingConfigurator.getConfigurationFile());
        } catch (Exception e) {
            simpleLocalLogger.log("Failed to configure logging");
        }
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