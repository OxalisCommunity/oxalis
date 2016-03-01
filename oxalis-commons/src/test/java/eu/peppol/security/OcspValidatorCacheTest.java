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

        cache.setTimoutForTesting(500);
        BigInteger serialNumber = new BigInteger("1000");

        assertEquals(cache.isKnownValidCertificate(serialNumber), false);

        cache.setKnownValidCertificate(serialNumber);
        assertEquals(cache.isKnownValidCertificate(serialNumber), true);

        Thread.sleep(5);
        assertEquals(cache.isKnownValidCertificate(serialNumber), true);

        Thread.sleep(600);
        assertEquals(cache.isKnownValidCertificate(serialNumber), false);
    }
}
