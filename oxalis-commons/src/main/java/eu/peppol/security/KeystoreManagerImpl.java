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

package eu.peppol.security;

import com.google.inject.Inject;
import eu.peppol.util.GlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.cert.X509Certificate;

/**
 * Handles operations related to <em>our</em> PEPPOL key and trust stores.
 * <p>
 * Author: nigel
 * Date: Oct 9, 2011
 * Time: 4:01:31 PM
 *
 * @author steinar@sendregning.no
 */
public class KeystoreManagerImpl implements KeystoreManager {

    private static final Logger log = LoggerFactory.getLogger(KeystoreManagerImpl.class);

    /**
     * Holds the keystore containing our (this access point) private key and public certificate.
     */
    private final KeyStore ourKeystore;

    /**
     * Holds the PEPPOL trust store, which contains the intermediate certificates and root certificates of PEPPOL
     */
    private KeyStore peppolTrustedKeyStore;
    private PrivateKey privateKey;
    private GlobalConfiguration globalConfiguration;

    @Inject
    public KeystoreManagerImpl(GlobalConfiguration globalConfiguration, KeystoreLoader keystoreLoader) {

        this.globalConfiguration = globalConfiguration;
        this.peppolTrustedKeyStore = keystoreLoader.loadTruststore();

        ourKeystore = keystoreLoader.loadOurCertificateKeystore();
        privateKey = loadOurPrivateKey(ourKeystore, globalConfiguration.getKeyStorePassword());
    }


    /**
     * Provides the currently loaded PEPPOL trust store holding the root and intermediate certificates.
     * The actual key store loaded, depends upon the global configuration.
     *
     * @return currently loaded truststore.
     */
    @Override
    public KeyStore getPeppolTrustedKeyStore() {
        return peppolTrustedKeyStore;
    }

    /**
     * Provides this PEPPOL Access Point's keystore, which holds the private key and the public certificate
     * issued by a PEPPOL authority. The physical location is referenced in the global configuration.
     *
     * @return the KeyStore holding the private key and certificate (with public key) of this access point
     */
    @Override
    public KeyStore getOurKeystore() {
        if (ourKeystore == null) {
            throw new IllegalStateException("KeystoreManagerImpl not properly initialized");
        }
        return ourKeystore;
    }


    /**
     * Retrieves the Access Point's certificate from the currently loaded keystore.
     *
     * @return the X.509 certificate identifying this access point
     */
    @Override
    public X509Certificate getOurCertificate() {

        try {
            KeyStore keystore = getOurKeystore();
            String alias = keystore.aliases().nextElement();
            return (X509Certificate) keystore.getCertificate(alias);

        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get our certificate from keystore", e);
        }
    }

    /**
     * Retrieves the Common Name attribute (CN=) of our certificate.
     *
     * @return the Common Name, without the CN= prefix, of our certificate
     */
    @Override
    public CommonName getOurCommonName() {
        return CommonName.valueOf(getOurCertificate().getSubjectX500Principal());
    }


    @Override
    public PrivateKey getOurPrivateKey() {
        return privateKey;
    }


    PrivateKey loadOurPrivateKey(KeyStore keyStore, String password) {
        try {
            String alias = keyStore.aliases().nextElement();
            Key key = keyStore.getKey(alias, password.toCharArray());

            if (key instanceof PrivateKey) {
                return (PrivateKey) key;
            } else {
                throw new RuntimeException("Private key must be first element in our keystore at " + globalConfiguration.getKeyStoreFileName() + " " + key.getClass());
            }
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Unable to access keystore: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new IllegalStateException("Unable to retrieve private key: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isOurCertificate(X509Certificate candidate) {
        X509Certificate ourCertificate = getOurCertificate();
        return ourCertificate.getSerialNumber().equals(candidate.getSerialNumber());
    }
}
