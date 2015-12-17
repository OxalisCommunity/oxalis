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

package eu.peppol.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;
import eu.peppol.statistics.RawStatisticsRepositoryFactoryProvider;

/**
 * Google Guice module which configures the repository (persistence) layer of our application.
 *
 * TODO: refactor repository module into separate modules for messages and statistics.
 *
 * @author steinar
 *         Date: 09.06.13
 *         Time: 21:46
 */
public class RepositoryModule extends AbstractModule {

    @Override
    protected void configure() {

    }


    @Provides @Singleton
    MessageRepository provideMessageRepository(MessageRepositoryFactory messageRepositoryFactory) {

        MessageRepository instance = messageRepositoryFactory.getInstanceWithDefault();
        return instance;
    }

    @Provides @Singleton
    RawStatisticsRepository provideStatisticsRepository() {
        RawStatisticsRepositoryFactory instance = RawStatisticsRepositoryFactoryProvider.getInstance();

        return instance.getInstanceForRawStatistics();
    }
}
