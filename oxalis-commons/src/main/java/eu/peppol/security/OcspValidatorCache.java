/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
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

    public static final Logger log = LoggerFactory.getLogger(OcspValidatorCache.class);

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
