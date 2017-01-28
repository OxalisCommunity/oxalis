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
import eu.peppol.util.GlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;

/**
 * Loads the key stores holding our access point certificate and
 * the PEPPOL trust store based upon the PKI version and the mode of operation.
 * <p>
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
    public KeyStore loadOurCertificateKeystore() {
        String keyStoreFileName = globalConfiguration.getKeyStoreFileName();
        String keyStorePassword = globalConfiguration.getKeyStorePassword();
        log.debug("Loading PEPPOL keystore from " + keyStoreFileName);
        return KeyStoreUtil.loadJksKeystore(keyStoreFileName, keyStorePassword);
    }
}
