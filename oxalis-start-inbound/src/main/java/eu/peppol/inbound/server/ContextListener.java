package eu.peppol.inbound.server;

import eu.peppol.inbound.util.Log;
import eu.peppol.start.identifier.Configuration;
import eu.peppol.start.identifier.KeystoreManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

/**
 * User: nigel
 * Date: Oct 24, 2011
 * Time: 3:08:30 PM
 */
@SuppressWarnings({"AccessStaticViaInstance"})
public class ContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {

        event.getServletContext().log("Oxalis messages are emitted using SLF4J, search for oxalis.log");

        Log.info("Starting Oxalis Access Point");
        Log.debug("Initialising keystore");

        try {
            KeystoreManager keystoreManager = new KeystoreManager();
            Configuration configuration = Configuration.getInstance();

            File keystore = locateKeystore(configuration);

            String keystorePassword = configuration.getKeyStorePassword();
            keystoreManager.initialiseKeystore(keystore, keystorePassword);
            Log.debug("Keystore initialised from " + keystore);
        } catch (RuntimeException e) {
            Log.error("Unable to initialize: " + e, e);

            // Shoves a decent error message into the Tomcat log
            event.getServletContext().log("ERROR: Unable to initialize: " + e, e);
            throw e;
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
}

