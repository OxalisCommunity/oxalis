package eu.peppol.inbound;

import org.testng.annotations.Test;

public class RunJettyServer extends AbstractJettyServerTest {

    @Test(groups = "manual", enabled = false)
    public void runServer() throws Exception {
        server.join();
    }
}
