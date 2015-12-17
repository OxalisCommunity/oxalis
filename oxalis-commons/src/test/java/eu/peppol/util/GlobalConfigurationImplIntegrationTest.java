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

package eu.peppol.util;

import com.google.inject.Inject;
import eu.peppol.security.PkiVersion;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 23.05.13
 *         Time: 22:28
 */
@Test(groups = "integration")
@Guice(modules = {OxalisCommonsModule.class})
public class GlobalConfigurationImplIntegrationTest {

    @Inject
    GlobalConfiguration globalConfiguration;

    @BeforeMethod
    public void initializeGlobalConfiguration() {
        assertNotNull(globalConfiguration);
    }

    @Test
    public void overrideDefaultPropertyValue() throws Exception {

        String inboundMessageStore = globalConfiguration.getInboundMessageStore();
        assertNotNull(inboundMessageStore, "Default value for " + PropertyDef.INBOUND_MESSAGE_STORE.name() + " not initialized");
    }

    @Test
    public void testTrustStorePassword() throws Exception {
        String trustStorePassword = globalConfiguration.getTrustStorePassword();
        assertNotNull(trustStorePassword);
        assertTrue(trustStorePassword.trim().length() > 0);
    }


    @Test
    public void testPkiVersion() throws Exception {
        PkiVersion pkiVersion = globalConfiguration.getPkiVersion();
        assertNotNull(pkiVersion);

    }

    @Test void testGetDefaultValidationQuery() {
        String validationQuery = globalConfiguration.getValidationQuery();
        assertNotNull(validationQuery);
        assertEquals(validationQuery, "select 1");

    }
}
