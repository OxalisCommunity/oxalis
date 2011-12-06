package eu.peppol.inbound.ocsp;

import eu.peppol.inbound.util.TestBase;
import org.testng.annotations.Test;

import java.math.BigInteger;

import static org.testng.Assert.*;

/**
 * User: nigel
 * Date: Dec 6, 2011
 * Time: 9:09:13 PM
 */
@Test
public class OcspValidatorCacheTest extends TestBase {

    public void test01() throws Exception {

        OcspValidatorCache cache = new OcspValidatorCache();
        cache.setTimoutForTesting(10);
        BigInteger serialNumber = new BigInteger("1000");
        assertEquals(cache.isKnownValidCertificate(serialNumber), false);

        cache.setKnownValidCertificate(serialNumber);
        assertEquals(cache.isKnownValidCertificate(serialNumber), true);

        Thread.sleep(5);
        assertEquals(cache.isKnownValidCertificate(serialNumber), true);

        Thread.sleep(10);
        assertEquals(cache.isKnownValidCertificate(serialNumber), false);
    }
}
