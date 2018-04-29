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

package no.difi.oxalis.commons.security;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import no.difi.oxalis.api.lang.OxalisLoadingException;
import no.difi.oxalis.api.model.AccessPointIdentifier;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.commons.guice.OxalisModule;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * @author erlend
 * @since 4.0.0
 */
public class CertificateModule extends OxalisModule {

    @Override
    protected void configure() {
        bindSettings(KeyStoreConf.class);
    }

    @Provides
    @Singleton
    protected KeyStore getKeyStore(Settings<KeyStoreConf> settings, @Named("conf") Path confFolder) throws Exception {
        KeyStore keystore = KeyStore.getInstance("JKS");

        Path path = settings.getPath(KeyStoreConf.PATH, confFolder);
        if (Files.notExists(path))
            throw new OxalisLoadingException(String.format("Unable to find keystore at '%s'.", path));

        try (InputStream inputStream = Files.newInputStream(path)) {
            keystore.load(inputStream, settings.getString(KeyStoreConf.PASSWORD).toCharArray());
        }
        return keystore;
    }

    @Provides
    @Singleton
    protected PrivateKey getPrivateKeyEntry(KeyStore keyStore, Settings<KeyStoreConf> settings) throws Exception {
        if (!keyStore.containsAlias(settings.getString(KeyStoreConf.KEY_ALIAS)))
            throw new OxalisLoadingException(String.format("Key alias '%s' is not found in the key store.", settings.getString(KeyStoreConf.KEY_ALIAS)));

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(
                settings.getString(KeyStoreConf.KEY_ALIAS),
                settings.getString(KeyStoreConf.KEY_PASSWORD).toCharArray());

        if (privateKey == null)
            throw new OxalisLoadingException("Unable to load private key due to wrong password.");

        return privateKey;
    }

    @Provides
    @Singleton
    protected X509Certificate getCertificate(KeyStore keyStore, Settings<KeyStoreConf> settings) throws Exception {
        if (!keyStore.containsAlias(settings.getString(KeyStoreConf.KEY_ALIAS)))
            throw new OxalisLoadingException(String.format("Key alias '%s' is not found in the key store.", settings.getString(KeyStoreConf.KEY_ALIAS)));

        return (X509Certificate) keyStore.getCertificate(settings.getString(KeyStoreConf.KEY_ALIAS));
    }

    @Provides
    @Singleton
    protected AccessPointIdentifier provideOurAccessPointIdentifier(X509Certificate certificate) {
        return new AccessPointIdentifier(CertificateUtils.extractCommonName(certificate));
    }

    @Provides
    @Singleton
    protected KeyStore.PrivateKeyEntry getPrivateKey(PrivateKey privateKey, X509Certificate certificate) {
        return new KeyStore.PrivateKeyEntry(
                privateKey,
                new Certificate[]{certificate}
        );
    }
}
