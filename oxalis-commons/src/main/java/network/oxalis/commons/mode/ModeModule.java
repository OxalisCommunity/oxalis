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

package network.oxalis.commons.mode;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.pkix.ocsp.api.OcspFetcher;
import network.oxalis.commons.certvalidator.api.CrlCache;
import network.oxalis.commons.certvalidator.api.CrlFetcher;
import network.oxalis.commons.certvalidator.util.SimpleCrlCache;
import network.oxalis.api.lang.OxalisLoadingException;
import network.oxalis.commons.guice.OxalisModule;
import network.oxalis.vefa.peppol.common.lang.PeppolLoadingException;
import network.oxalis.vefa.peppol.mode.Mode;
import network.oxalis.vefa.peppol.security.api.CertificateValidator;
import org.apache.http.client.config.RequestConfig;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * @author erlend
 * @since 4.0.0
 *
 * @author aaron-kumar
 * @since 5.0.0
 *
 */
@Slf4j
public class ModeModule extends OxalisModule {

    @Override
    protected void configure() {
        bind(OcspFetcher.class).to(OxalisOcspFetcher.class);
        bind(CrlCache.class).toInstance(new SimpleCrlCache());
        bind(CrlFetcher.class).to(OxalisCrlFetcher.class);

        bind(Mode.class)
                .toProvider(ModeProvider.class)
                .asEagerSingleton();
    }

    @Provides
    @Singleton
    protected CertificateValidator getCertificateValidator(Mode mode) throws PeppolLoadingException {
        return mode.initiate("security.validator.class", CertificateValidator.class);
    }

    @Provides
    @Singleton
    @Named("certificate")
    protected RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(10 * 1000)
                .setConnectionRequestTimeout(10 * 1000)
                .setSocketTimeout(10 * 1000)
                .build();
    }

    /**
     * @since 4.0.3
     */
    @Provides
    @Singleton
    @Named("truststore-ap")
    protected KeyStore getTruststoreAp(Mode mode) {
        try (InputStream inputStream = getClass().getResourceAsStream(mode.getString("security.truststore.ap"))) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(inputStream, mode.getString("security.truststore.password").toCharArray());
            return keyStore;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new OxalisLoadingException("Unable to load truststore for AP.", e);
        }
    }

    /**
     * @since 4.0.3
     */
    @Provides
    @Singleton
    @Named("truststore-smp")
    protected KeyStore getTruststoreSmp(Mode mode) {
        try (InputStream inputStream = getClass().getResourceAsStream(mode.getString("security.truststore.smp"))) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(inputStream, mode.getString("security.truststore.password").toCharArray());
            return keyStore;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new OxalisLoadingException("Unable to load truststore for SMP.", e);
        }
    }
}
