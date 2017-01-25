/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.peppol.security;

import com.google.inject.Inject;
import eu.peppol.util.OperationalMode;
import eu.peppol.util.OxalisCommonsTestModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.security.KeyStore;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 08.08.13
 *         Time: 20:19
 */
@Guice(modules = {OxalisCommonsTestModule.class})
public class PeppolKeystoreLoaderTest {

    @Inject
    PeppolKeystoreLoader peppolKeystoreLoader;

    @BeforeMethod
    public void setUp() {
//        peppolKeystoreLoader = new PeppolKeystoreLoader(UnitTestGlobalConfigurationImpl.createInstance());
    }

    @Test
    public void loadKeystores() throws Exception {
        KeyStore keyStore = peppolKeystoreLoader.loadOurCertificateKeystore();
        assertNotNull(keyStore,"Certificate key store not loaded");
    }
}
