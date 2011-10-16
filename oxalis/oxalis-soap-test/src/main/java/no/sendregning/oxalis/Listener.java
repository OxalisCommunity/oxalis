package no.sendregning.oxalis;

import eu.peppol.start.util.Configuration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * User: nigel
 * Date: Oct 7, 2011
 * Time: 8:35:44 PM
 */
public class Listener implements ServletContextListener {

    private TestDaemon testDaemon = new TestDaemon();

    public void contextInitialized(ServletContextEvent event) {

        if (Configuration.getInstance().getProperty("run.self.test").equals("true")) {
            testDaemon.start();
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        Log.info("Stopping TestDaemon");
        testDaemon.stop();
    }
}
