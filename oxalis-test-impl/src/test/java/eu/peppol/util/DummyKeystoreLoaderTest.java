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
        assertNotNull(certificateKeystore);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void invalidKeystore() throws Exception {
        new DummyKeystoreLoader().loadKeystore("/fake-oxalis-global.properties");
    }
}