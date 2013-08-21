package eu.peppol.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 23.05.13
 *         Time: 22:28
 */
@Test(groups = "integration")
public class GlobalConfigurationIntegrationTest {

    private GlobalConfiguration globalConfiguration;

    @BeforeMethod
    public void initializeGlobalConfiguration() {
        globalConfiguration = GlobalConfiguration.getInstance();
    }

    @Test
    public void testLogProperties() throws Exception {
        globalConfiguration.logProperties();
    }

    @Test
    public void overrideDefaultPropertyValue() throws Exception {
        String inboundMessageStore = globalConfiguration.getInboundMessageStore();
        assertNotNull(inboundMessageStore, "Default value for " + GlobalConfiguration.PropertyDef.INBOUND_MESSAGE_STORE.name() + " not initialized");
    }

    @Test
    public void testTrustStorePassword() throws Exception {
        String trustStorePassword = globalConfiguration.getTrustStorePassword();
        assertNotNull(trustStorePassword);
        assertTrue(trustStorePassword.trim().length() > 0);
    }
}
