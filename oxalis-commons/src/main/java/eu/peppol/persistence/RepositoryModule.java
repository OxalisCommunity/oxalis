/*
 * Copyright (c) 2015 Steinar Overbeck Cook
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
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
    MessageRepository provideMessageRepository() {
        MessageRepository instance = MessageRepositoryFactory.getInstance();
        return instance;
    }

    @Provides @Singleton
    RawStatisticsRepository provideStatisticsRepository() {
        RawStatisticsRepositoryFactory instance = RawStatisticsRepositoryFactoryProvider.getInstance();

        return instance.getInstanceForRawStatistics();
    }
}
