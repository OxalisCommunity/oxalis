package eu.peppol.inbound.server;

import eu.peppol.inbound.util.Log;
import eu.peppol.start.util.Configuration;
import eu.peppol.start.util.KeystoreManager;

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

        Log.info("Starting Oxalis Access Point");

        KeystoreManager keystoreManager = new KeystoreManager();
        Configuration configuration = Configuration.getInstance();
        File keystore = new File(configuration.getProperty("keystore"));
        String keystorePassword = configuration.getProperty("keystore.password");
        keystoreManager.initialiseKeystore(keystore, keystorePassword);
    }

    public void contextDestroyed(ServletContextEvent event) {
        Log.info("Stopping Oxalis Access Point");
    }
}

