package eu.peppol.util;

import com.google.inject.Inject;
import eu.peppol.security.PkiVersion;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 23.05.13
 *         Time: 22:28
 */
@Test(groups = "integration")
@Guice(modules = {RuntimeConfigurationModule.class})
public class GlobalConfigurationImplIntegrationTest {

    @Inject
    GlobalConfiguration globalConfiguration;

    @BeforeMethod
    public void initializeGlobalConfiguration() {
        assertNotNull(globalConfiguration);
    }

    @Test
    public void overrideDefaultPropertyValue() throws Exception {

        String inboundMessageStore = globalConfiguration.getInboundMessageStore();
        assertNotNull(inboundMessageStore, "Default value for " + GlobalConfigurationImpl.PropertyDef.INBOUND_MESSAGE_STORE.name() + " not initialized");
    }

    @Test
    public void testTrustStorePassword() throws Exception {
        String trustStorePassword = globalConfiguration.getTrustStorePassword();
        assertNotNull(trustStorePassword);
        assertTrue(trustStorePassword.trim().length() > 0);
    }


    @Test
    public void testPkiVersion() throws Exception {
        PkiVersion pkiVersion = globalConfiguration.getPkiVersion();
        assertNotNull(pkiVersion);

    }

    @Test void testGetDefaultValidationQuery() {
        String validationQuery = globalConfiguration.getValidationQuery();
        assertNotNull(validationQuery);
        assertEquals(validationQuery, "select 1");

    }
}
