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

package eu.peppol.inbound.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.statistics.RawStatisticsRepository;

/**
 * RepositoryModule used to inject specific instances of various repository objects.
 *
 * @author steinar
 *         Date: 09.06.13
 *         Time: 23:57
 */
public class TestRepositoryModule extends AbstractModule {


    private final MessageRepository messageRepository;
    private final RawStatisticsRepository rawStatisticsRepository;

    public TestRepositoryModule(MessageRepository messageRepository, RawStatisticsRepository rawStatisticsRepository) {

        this.messageRepository = messageRepository;
        this.rawStatisticsRepository = rawStatisticsRepository;
    }

    @Override
    protected void configure() {

    }

    @Provides
    MessageRepository provideMessageRepository() {
        return messageRepository;
    }

    @Provides
    RawStatisticsRepository provideStatisticsRepository(){
        return rawStatisticsRepository;
    }
}
