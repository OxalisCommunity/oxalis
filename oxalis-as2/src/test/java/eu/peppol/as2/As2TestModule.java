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

package eu.peppol.as2;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.security.KeystoreLoader;
import eu.peppol.security.KeystoreManager;
import eu.peppol.security.KeystoreManagerImpl;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.util.DummyKeystoreLoader;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.UnitTestGlobalConfigurationImpl;
import no.difi.oxalis.api.inbound.PayloadPersister;
import no.difi.oxalis.api.inbound.InboundVerifier;
import no.difi.oxalis.api.inbound.ReceiptPersister;
import no.difi.oxalis.commons.security.CertificateUtils;
import org.mockito.Mockito;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * @author steinar
 *         Date: 18.12.2015
 *         Time: 09.54
 */
public class As2TestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(KeystoreLoader.class).to(DummyKeystoreLoader.class).in(Singleton.class);
        bind(KeystoreManager.class).to(KeystoreManagerImpl.class).in(Singleton.class);

        bind(RawStatisticsRepository.class).toInstance(Mockito.mock(RawStatisticsRepository.class));
        bind(MessageRepository.class).toInstance(Mockito.mock(MessageRepository.class));

        bind(PayloadPersister.class).toInstance((mi, h, is) -> null);
        bind(ReceiptPersister.class).toInstance((m, p) -> null);
        bind(InboundVerifier.class).toInstance((mi, h) -> {
        });
    }

    @Provides
    @Singleton
    GlobalConfiguration provideGlobalConfiguration() {
        return UnitTestGlobalConfigurationImpl.createInstance();
    }

    @Provides
    @Singleton
    protected PrivateKey providePrivateKey(KeystoreManager keystoreManager) {
        return keystoreManager.getOurPrivateKey();
    }

    @Provides
    @Singleton
    protected X509Certificate provideCertificate(KeystoreManager keystoreManager) {
        return keystoreManager.getOurCertificate();
    }

    @Provides
    @Singleton
    protected AccessPointIdentifier provideAccessPointIdentifier(X509Certificate certificate) {
        return new AccessPointIdentifier(CertificateUtils.extractCommonName(certificate));
    }
}
