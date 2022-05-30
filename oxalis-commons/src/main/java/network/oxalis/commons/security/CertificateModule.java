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

package network.oxalis.commons.security;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.lang.OxalisLoadingException;
import network.oxalis.api.model.AccessPointIdentifier;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.guice.OxalisModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author erlend
 * @since 4.0.0
 */
@Slf4j
public class CertificateModule extends OxalisModule {

    @Override
    protected void configure() {
        bindSettings(KeyStoreConf.class);

        bind(KeyStore.PrivateKeyEntry.class)
                .toProvider(PrivateKeyEntryProvider.class)
                .asEagerSingleton();
    }

    @Provides
    @Singleton
    protected KeyStore getKeyStore(Settings<KeyStoreConf> settings, @Named("conf") Path confFolder) {
        Path path = settings.getPath(KeyStoreConf.PATH, confFolder);

        try {
            KeyStore keystore = KeyStore.getInstance("JKS");

            if (Files.notExists(path))
                throw new OxalisLoadingException(String.format("Unable to find keystore at '%s'.", path));

            try (InputStream inputStream = Files.newInputStream(path)) {
                keystore.load(inputStream, settings.getString(KeyStoreConf.PASSWORD).toCharArray());
            }
            return keystore;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new OxalisLoadingException("Something went wrong during handling of key store.", e);
        } catch (IOException e) {
            throw new OxalisLoadingException(String.format("Error during reading of '%s'.", path), e);
        }
    }

    @Provides
    @Singleton
    protected PrivateKey getPrivateKeyEntry(KeyStore keyStore, Settings<KeyStoreConf> settings) {
        try {
            if (!keyStore.containsAlias(settings.getString(KeyStoreConf.KEY_ALIAS)))
                throw new OxalisLoadingException(String.format(
                        "Key alias '%s' is not found in the key store.",
                        settings.getString(KeyStoreConf.KEY_ALIAS)));

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(
                    settings.getString(KeyStoreConf.KEY_ALIAS),
                    settings.getString(KeyStoreConf.KEY_PASSWORD).toCharArray());

            if (privateKey == null)
                throw new OxalisLoadingException("Unable to load private key due to wrong password.");

            return privateKey;
        } catch (UnrecoverableKeyException e) {
            throw new OxalisLoadingException("Unable to load private key due to wrong password.", e);
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            throw new OxalisLoadingException("Something went wrong during handling of key store.", e);
        }
    }

    @Provides
    @Singleton
    protected X509Certificate getCertificate(KeyStore keyStore, Settings<KeyStoreConf> settings) {
        try {
            if (!keyStore.containsAlias(settings.getString(KeyStoreConf.KEY_ALIAS)))
                throw new OxalisLoadingException(String.format(
                        "Key alias '%s' is not found in the key store.", settings.getString(KeyStoreConf.KEY_ALIAS)));

            X509Certificate certificate = (X509Certificate) keyStore.
                    getCertificate(settings.getString(KeyStoreConf.KEY_ALIAS));

            log.info("Certificate subject: {}", certificate.getSubjectX500Principal().toString());
            log.info("Certificate issuer: {}", certificate.getIssuerX500Principal().toString());
            // log.info("Certificate: {}", BaseEncoding.base64().encode(certificate.getEncoded()));

            return certificate;
        } catch (KeyStoreException e) {
            throw new OxalisLoadingException("Something went wrong during handling of key store.", e);
        }
    }

    @Provides
    @Singleton
    protected AccessPointIdentifier provideOurAccessPointIdentifier(X509Certificate certificate) {
        return new AccessPointIdentifier(CertificateUtils.extractCommonName(certificate));
    }
}
