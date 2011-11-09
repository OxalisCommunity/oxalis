package eu.peppol.start.util;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author $Author$ (of last change)
 *         Created by
 *         User: steinar
 *         Date: 09.11.11
 *         Time: 10:05
 */
public class ConfigurationTest {
    @org.junit.Test
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
        assertNotNull(Configuration.getInstance().getKeystoreFilename());
    }
}
