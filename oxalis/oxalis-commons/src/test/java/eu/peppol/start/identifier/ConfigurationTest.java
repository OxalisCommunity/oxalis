package eu.peppol.start.identifier;

import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.*;


/**
 * @author $Author$ (of last change)
 *         Created by
 *         User: steinar
 *         Date: 09.11.11
 *         Time: 10:05
 */
public class ConfigurationTest {
    @Test
    public void testGetProperty() throws Exception {


        Properties fallback = new Properties();
        Properties p = new Properties(fallback);

        fallback.setProperty("p1", "fallback");

        assertEquals("fallback", p.getProperty("p1"));

        p.setProperty("p1", "overridden");
        assertEquals("overridden", p.getProperty("p1"));
        assertEquals("fallback", fallback.getProperty("p1"));

        assertNull(System.getProperty("p1"));

    }

    @Test
    public void getKeyStorePath() {
        Configuration configuration = Configuration.getInstance();

        // This requires the presence of the resource named by Configuration#CUSTOM_PROPERTIES_PATH,
        // which needs to hold oxalis.keystore property
        assertNotNull(configuration.getKeyStoreFileName());

        assertEquals("TEST", configuration.getInboundMessageStore());
    }
    
}
