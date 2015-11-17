package eu.peppol.security;

import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import org.slf4j.Logger;
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

    private static final Logger log = LoggerFactory.getLogger(OcspValidatorCache.class);

    private static final boolean USE_CACHE = true;

    private static long timeout = 5*60 * 1000; // 5 minutes
    private static Map<BigInteger, Long> validCertificateCache = new ConcurrentHashMap<BigInteger, Long>();

    public  boolean isKnownValidCertificate(BigInteger serialNumber) {
        Long timestamp = validCertificateCache.get(serialNumber);
        if (timestamp != null) {
            log.debug("(System.currentTimeMillis() - timestamp)=" + (System.currentTimeMillis() - timestamp));
        }
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
