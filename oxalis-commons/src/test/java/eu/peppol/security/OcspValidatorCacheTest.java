package eu.peppol.security;

import eu.peppol.security.OcspValidatorCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.math.BigInteger;

import static org.testng.Assert.assertEquals;

/**
 * User: nigel
 * Date: Dec 6, 2011
 * Time: 9:09:13 PM
 */
@Test
public class OcspValidatorCacheTest  {

    private static final Logger log = LoggerFactory.getLogger(OcspValidatorCacheTest.class);

    public void test01() throws Exception {

        OcspValidatorCache cache = OcspValidatorCache.getInstance();

        cache.setTimoutForTesting(500);
        BigInteger serialNumber = new BigInteger("1000");

        assertEquals(cache.isKnownValidCertificate(serialNumber), false);

        cache.setKnownValidCertificate(serialNumber);
        log.debug("1. System.currentTimeMillis()=" + System.currentTimeMillis());
        assertEquals(cache.isKnownValidCertificate(serialNumber), true);

        Thread.sleep(5);
        log.debug("2. System.currentTimeMillis()=" + System.currentTimeMillis());
        assertEquals(cache.isKnownValidCertificate(serialNumber), true);

        Thread.sleep(600);
        log.debug("3. System.currentTimeMillis()=" + System.currentTimeMillis());
        assertEquals(cache.isKnownValidCertificate(serialNumber), false);
    }
}
