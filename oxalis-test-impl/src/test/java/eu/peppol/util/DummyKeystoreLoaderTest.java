/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Public Government and eGovernment (Difi)
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

import org.testng.annotations.Test;

import java.security.KeyStore;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 21.12.2015
 *         Time: 20.14
 */
public class DummyKeystoreLoaderTest {

    @Test
    public void testLoadOurCertificateKeystore() throws Exception {

        DummyKeystoreLoader dummyKeystoreLoader = new DummyKeystoreLoader();
        KeyStore certificateKeystore = dummyKeystoreLoader.loadOurCertificateKeystore();
        KeyStore trustedKeystore = dummyKeystoreLoader.loadTruststore();
        assertNotNull(certificateKeystore);
        assertNotNull(trustedKeystore);
    }
}