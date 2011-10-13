package no.sendregning.oxalis;

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
//        testDaemon.start();
    }

    public void contextDestroyed(ServletContextEvent event) {
        Log.info("Stopping TestDaemon");
        testDaemon.stop();
    }
}
