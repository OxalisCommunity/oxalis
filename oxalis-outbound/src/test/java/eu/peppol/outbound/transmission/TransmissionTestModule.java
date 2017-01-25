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

package eu.peppol.outbound.transmission;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.security.KeystoreLoader;
import eu.peppol.security.KeystoreManager;
import eu.peppol.security.KeystoreManagerImpl;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.StatisticsTransformer;
import eu.peppol.util.DummyKeystoreLoader;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OxalisKeystoreModule;
import eu.peppol.util.UnitTestGlobalConfigurationImpl;

import java.util.Date;

/**
 * Module which will provide the components needed for unit testing of the classes in
 * the eu.peppol.outbound.transmission package.
 *
 * @author steinar
 *         Date: 29.10.13
 *         Time: 11:42
 */
public class TransmissionTestModule extends OxalisKeystoreModule {

    @Override
    protected void configure() {
        bind(KeystoreLoader.class).to(DummyKeystoreLoader.class).in(Singleton.class);
        bind(KeystoreManager.class).to(KeystoreManagerImpl.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    GlobalConfiguration provideTestConfiguration() {
        return UnitTestGlobalConfigurationImpl.createInstance();
    }

    @Provides
    RawStatisticsRepository obtainRawStaticsRepository() {
        // Fake RawStatisticsRepository
        return new RawStatisticsRepository() {
            @Override
            public Integer persist(RawStatistics rawStatistics) {
                return null;
            }

            @Override
            public void fetchAndTransformRawStatistics(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity) {
            }
        };
    }
}
