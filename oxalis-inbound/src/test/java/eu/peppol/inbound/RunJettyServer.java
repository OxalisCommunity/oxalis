package eu.peppol.inbound;

import com.google.inject.Injector;
import eu.peppol.inbound.server.OxalisGuiceContextListener;
import no.difi.oxalis.test.jetty.AbstractJettyServerTest;
import org.testng.annotations.Test;

public class RunJettyServer extends AbstractJettyServerTest {

    @Override
    public Injector getInjector() {
        return new OxalisGuiceContextListener().getInjector();
    }

    @Test(groups = "manual", enabled = false)
    public void runServer() throws Exception {
        server.join();
    }
}
