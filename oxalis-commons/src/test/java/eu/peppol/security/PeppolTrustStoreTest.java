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

import eu.peppol.util.OperationalMode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 08.08.13
 *         Time: 20:19
 */
public class PeppolTrustStoreTest {

    PeppolTrustStore peppolTrustStore;

    @BeforeMethod
    public void setUp() {
        peppolTrustStore = new PeppolTrustStore();
    }

    @Test
    public void version1InProductionMode() throws Exception {

        // In Transitional version and Production mode, two keystores should be loaded
        List<PeppolTrustStore.TrustStoreResource> trustStoreResources = peppolTrustStore.resourceNamesFor(PkiVersion.V1, OperationalMode.PRODUCTION);
        assertEquals(trustStoreResources.size(), 1);

        assertTrue(trustStoreResources.contains(PeppolTrustStore.TrustStoreResource.V2_TEST));
    }


    @Test
    public void transitionalVersionInProductionMode() throws Exception {

        // In Transitional version and Production mode, two keystores should be loaded
        List<PeppolTrustStore.TrustStoreResource> trustStoreResources = peppolTrustStore.resourceNamesFor(PkiVersion.T, OperationalMode.PRODUCTION);
        assertEquals(trustStoreResources.size(), 2);

        assertTrue(trustStoreResources.contains(PeppolTrustStore.TrustStoreResource.V2_PRODUCTION));
        assertTrue(trustStoreResources.contains(PeppolTrustStore.TrustStoreResource.V2_TEST));

    }

    @Test
    public void version2InProductionMode() throws Exception {

        // In Transitional version and Production mode, two keystores should be loaded
        List<PeppolTrustStore.TrustStoreResource> trustStoreResources = peppolTrustStore.resourceNamesFor(PkiVersion.V2, OperationalMode.PRODUCTION);
        assertEquals(trustStoreResources.size(), 1);

        assertTrue(trustStoreResources.contains(PeppolTrustStore.TrustStoreResource.V2_PRODUCTION));
    }

}
