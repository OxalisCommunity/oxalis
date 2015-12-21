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

import com.google.inject.Inject;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the key stores holding our access point certificate and
 * the PEPPOL trust store based upon the PKI version and the mode of operation.
 *
 * This implementation holds the information on the location
 * of the trust stores residing in our class path and the fact that
 * the keystore is referenced by the global configuration.
 *
 * @author steinar
 *         Date: 08.08.13
 *         Time: 19:49
 */
public class PeppolKeystoreLoader implements KeystoreLoader {

    public static final Logger log = LoggerFactory.getLogger(PeppolKeystoreLoader.class);
    private final GlobalConfiguration globalConfiguration;

    @Inject
    public PeppolKeystoreLoader(GlobalConfiguration globalConfiguration) {

        this.globalConfiguration = globalConfiguration;
    }

    @Override
    public KeyStore loadTruststore() {
        return loadTrustStoreFor(globalConfiguration.getTrustStorePassword(), globalConfiguration.getModeOfOperation());
    }

    @Override
    public KeyStore loadOurCertificateKeystore() {
        String keyStoreFileName = globalConfiguration.getKeyStoreFileName();
        String keyStorePassword = globalConfiguration.getKeyStorePassword();
        log.debug("Loading PEPPOL keystore from " + keyStoreFileName);
        return KeyStoreUtil.loadJksKeystore(keyStoreFileName, keyStorePassword);
    }


    /**
     * Combines and loads the built-in PEPPOL trust stores, assuming they all have the same password.
     *
     * @param operationalMode
     * @return
     */
    KeyStore loadTrustStoreFor(String trustStorePassword, OperationalMode operationalMode) {

        // Figures out which trust store resources to load depending upon the mode of operation and
        // which PKI version we are using.
        List<TrustStoreResource> trustStoreResources = resourceNamesFor(operationalMode);

        List<String> resourceNames = fetchResourceNames(trustStoreResources);

        if (log.isDebugEnabled()) {

            StringBuilder sb = new StringBuilder("Loading and combining trust stores:");
            for (String resourceName : resourceNames) {
                sb.append(" ").append(resourceName);
            }
            log.debug(sb.toString());
        }

        List<KeyStore> trustStores = KeyStoreUtil.loadKeyStores(resourceNames, trustStorePassword);

        KeyStore keyStore = KeyStoreUtil.combineTrustStores(trustStores.toArray(new KeyStore[]{}));

        return keyStore;
    }


    List<String> fetchResourceNames(List<TrustStoreResource> trustStoreResources) {
        List<String> resourceNames = new ArrayList<String>();
        for (TrustStoreResource trustStoreResource : trustStoreResources) {
            resourceNames.add(trustStoreResource.getResourcename());
        }
        return resourceNames;
    }


    List<TrustStoreResource> resourceNamesFor(OperationalMode operationalMode) {
        List<TrustStoreResource> trustStoresToLoad = new ArrayList<TrustStoreResource>();
        switch (operationalMode) {
            case TEST:
                trustStoresToLoad.add(TrustStoreResource.V2_TEST);
                break;

            case PRODUCTION:
                trustStoresToLoad.add(TrustStoreResource.V2_PRODUCTION);
                break;
            default:
                throw new IllegalStateException("No configuration for operational mode " + operationalMode.name());
        }
        return trustStoresToLoad;
    }


    enum TrustStoreResource {
        V2_TEST("truststore-test.jks"),
        V2_PRODUCTION("truststore-production.jks"),;

        private final String resourceName;

        TrustStoreResource(String s) {
            this.resourceName = s;
        }

        public String getResourcename() {
            return resourceName;
        }
    }
}
