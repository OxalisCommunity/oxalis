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

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 12.12.2015
 *         Time: 00.05
 */
public class OxalisCommonsModuleTest {

    @Test(groups = {"integration"})
    public void testRuntimeConfigurationModule() throws Exception {

        Injector injector = Guice.createInjector(new OxalisCommonsModule());
        KeystoreManager keystoreManager = injector.getInstance(KeystoreManager.class);
        CommonName ourCommonName = keystoreManager.getOurCommonName();
        assertNotNull(ourCommonName.toString());


    }
}