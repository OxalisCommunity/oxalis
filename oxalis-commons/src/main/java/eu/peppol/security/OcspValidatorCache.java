package eu.peppol.security;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * User: nigel
 * Date: Dec 6, 2011
 * Time: 9:08:50 PM
 */
@SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
public class OcspValidatorCache {

    private static final boolean USE_CACHE = true;

    private static long timeout = 60 * 1000; // One minute
    private static Map<BigInteger, Long> validCertificateCache = new HashMap<BigInteger, Long>();

    public synchronized boolean isKnownValidCertificate(BigInteger serialNumber) {
        Long timestamp = validCertificateCache.get(serialNumber);
        return timestamp != null && (System.currentTimeMillis() - timestamp) < timeout;
    }

    public synchronized void setKnownValidCertificate(BigInteger serialNumber) {
        if (USE_CACHE) {
            validCertificateCache.put(serialNumber, System.currentTimeMillis());
        }
    }

    void setTimoutForTesting(long timeoutValue) {
        timeout = timeoutValue;
    }
}
