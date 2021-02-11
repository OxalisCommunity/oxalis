/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.test.dummy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import network.oxalis.api.model.AccessPointIdentifier;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class DummyPkiModule extends AbstractModule {

    @Override
    protected void configure() {
        // No action.
    }

    @Provides
    @Singleton
    protected KeyStore getKeyStore() throws Exception {
        KeyStore keystore = KeyStore.getInstance("JKS");
        try (InputStream inputStream = getClass().getResourceAsStream("/dummy/app_0000000001.jks")) {
            keystore.load(inputStream, "changeit".toCharArray());
        }
        return keystore;
    }

    @Provides
    @Singleton
    protected PrivateKey getPrivateKeyEntry(KeyStore keyStore) throws Exception {
        return (PrivateKey) keyStore.getKey("ap", "changeit".toCharArray());
    }

    @Provides
    @Singleton
    protected X509Certificate getCertificate(KeyStore keyStore) throws Exception {
        return (X509Certificate) keyStore.getCertificate("ap");
    }

    @Provides
    @Singleton
    protected AccessPointIdentifier provideOurAccessPointIdentifier(X509Certificate certificate) {
        return new AccessPointIdentifier("APP_0000000001");
    }

    @Provides
    @Singleton
    protected KeyStore.PrivateKeyEntry getPrivateKey(PrivateKey privateKey, X509Certificate certificate) throws Exception {
        return new KeyStore.PrivateKeyEntry(
                privateKey,
                new Certificate[]{certificate}
        );
    }
}

