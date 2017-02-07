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
import eu.peppol.identifier.AccessPointIdentifier;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.commons.settings.SettingsBuilder;

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
        SettingsBuilder.with(binder(), KeyStoreConf.class);
    }

    @Provides
    @Singleton
    protected KeyStore getKeyStore(Settings<KeyStoreConf> settings) throws Exception {
        KeyStore keystore = KeyStore.getInstance("JKS");
        try (InputStream inputStream = Files.newInputStream(Paths.get(settings.getString(KeyStoreConf.PATH)))) {
            keystore.load(inputStream, settings.getString(KeyStoreConf.PASSWORD).toCharArray());
        }
        return keystore;
    }

    @Provides
    @Singleton
    protected PrivateKey getPrivateKeyEntry(KeyStore keyStore, Settings<KeyStoreConf> settings) throws Exception {
        return (PrivateKey) keyStore.getKey(
                settings.getString(KeyStoreConf.KEY_ALIAS),
                settings.getString(KeyStoreConf.KEY_PASSWORD).toCharArray());
    }

    @Provides
    @Singleton
    protected X509Certificate getCertificate(KeyStore keyStore, Settings<KeyStoreConf> settings) throws Exception {
        return (X509Certificate) keyStore.getCertificate(settings.getString(KeyStoreConf.KEY_ALIAS));
    }

    @Provides
    @Singleton
    protected AccessPointIdentifier provideOurAccessPointIdentifier(X509Certificate certificate) {
        return new AccessPointIdentifier(CertificateUtils.extractCommonName(certificate));
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
