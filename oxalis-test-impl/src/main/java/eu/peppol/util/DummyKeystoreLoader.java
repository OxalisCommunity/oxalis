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

import eu.peppol.security.KeystoreLoader;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 *
 * @author steinar
 *         Date: 21.12.2015
 *         Time: 17.33
 */
public class DummyKeystoreLoader implements KeystoreLoader {


    public static final String DUMMY_CA_RESOURCE_NAME = "security/oxalis-dummy-ca.jks";
    public static final String OUR_CERTIFICATE_KEYSTORE_RESOURCE = "security/oxalis-dummy-keystore.jks";

    @Override
    public KeyStore loadTruststore() {
        return loadKeystore(DUMMY_CA_RESOURCE_NAME);
    }

    @Override
    public KeyStore loadOurCertificateKeystore() {
        return loadKeystore(OUR_CERTIFICATE_KEYSTORE_RESOURCE);
    }

    KeyStore loadKeystore(String resourceName) {
        try (InputStream is = DummyKeystoreLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(is, "peppol".toCharArray());
            return keyStore;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to load our AP certificate key store. " + e.getMessage(), e);
        }

    }
}
