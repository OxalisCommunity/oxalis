/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
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

package eu.peppol.inbound.guice;

import com.google.inject.AbstractModule;
import eu.peppol.start.persistence.MessageRepository;
import eu.peppol.start.persistence.MessageRepositoryFactory;
import eu.peppol.statistics.StatisticsRepository;
import eu.peppol.statistics.StatisticsRepositoryFactoryProvider;

/**
 * @author steinar
 *         Date: 09.06.13
 *         Time: 15:10
 */
public class WebServiceModule extends AbstractModule {

    @Override
    protected void configure() {

        // Persistence of the message
        bind(MessageRepository.class).toInstance(MessageRepositoryFactory.getInstance());
        // Persistence of the statistics
        bind(StatisticsRepository.class).toInstance(StatisticsRepositoryFactoryProvider.getInstance().getInstance());
    }

}
