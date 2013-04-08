package net.sf.ehcache;

import eu.peppol.start.identifier.AccessPointIdentifier;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author steinar
 *         Date: 08.04.13
 *         Time: 14:08
 */
public class EhCacheTest {

    @Test
    public void createSampleCaches() throws Exception {

        CacheManager cacheManager = CacheManager.create();

        Ehcache ehcache = cacheManager.addCacheIfAbsent(AccessPointIdentifier.class.getSimpleName());

        Element element = ehcache.get(AccessPointIdentifier.TEST);
        assertNull(element);

        Integer value = new Integer(42);
        ehcache.put(new Element(AccessPointIdentifier.TEST, value));

        Element element2 = ehcache.get(AccessPointIdentifier.TEST);

        assertEquals(element2.getValue(),value);
    }
}
