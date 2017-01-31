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

package no.difi.oxalis.commons.security;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import eu.peppol.identifier.AccessPointIdentifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class CertificateModule extends AbstractModule {

    @Override
    protected void configure() {
        // No action.
    }

    @Provides
    @Singleton
    protected KeyStore getKeyStore(Config config) throws Exception {
        KeyStore keystore = KeyStore.getInstance("JKS");
        try (InputStream inputStream = Files.newInputStream(Paths.get(config.getString("keystore.path")))) {
            keystore.load(inputStream, config.getString("keystore.password").toCharArray());
        }
        return keystore;
    }

    @Provides
    @Singleton
    protected PrivateKey getPrivateKeyEntry(KeyStore keyStore, Config config) throws Exception {
        return (PrivateKey) keyStore.getKey(
                config.getString("keystore.key.alias"),
                config.getString("keystore.key.password").toCharArray());
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
