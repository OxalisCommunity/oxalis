package eu.peppol.security;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: nigel
 * Date: Dec 6, 2011
 * Time: 9:08:50 PM
 */
@SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
public enum OcspValidatorCache {

    INSTANCE;

    static public OcspValidatorCache getInstance() {
        return INSTANCE;
    }


    private static final boolean USE_CACHE = true;

    private static long timeout = 5*60 * 1000; // 5 minutes
    private static Map<BigInteger, Long> validCertificateCache = new ConcurrentHashMap<BigInteger, Long>();

    public  boolean isKnownValidCertificate(BigInteger serialNumber) {
        Long timestamp = validCertificateCache.get(serialNumber);
        return timestamp != null && (System.currentTimeMillis() - timestamp) <= timeout;
    }

    public  void setKnownValidCertificate(BigInteger serialNumber) {
        if (USE_CACHE) {
            validCertificateCache.put(serialNumber, System.currentTimeMillis());
        }
    }

    void setTimoutForTesting(long timeoutValue) {
        timeout = timeoutValue;
    }
}
