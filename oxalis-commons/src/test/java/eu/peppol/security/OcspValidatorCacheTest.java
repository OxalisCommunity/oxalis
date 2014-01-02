package eu.peppol.security;

import eu.peppol.security.OcspValidatorCache;
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

    public void test01() throws Exception {

        OcspValidatorCache cache = OcspValidatorCache.getInstance();

        cache.setTimoutForTesting(20);
        BigInteger serialNumber = new BigInteger("1000");

        assertEquals(cache.isKnownValidCertificate(serialNumber), false);

        cache.setKnownValidCertificate(serialNumber);
        assertEquals(cache.isKnownValidCertificate(serialNumber), true);

        Thread.sleep(5);
        assertEquals(cache.isKnownValidCertificate(serialNumber), true);

        Thread.sleep(21);
        assertEquals(cache.isKnownValidCertificate(serialNumber), false);
    }
}
