/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language
 *  governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.outbound.transmission;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import no.difi.oxalis.api.outbound.TransmissionService;
import no.difi.oxalis.api.outbound.Transmitter;
import no.difi.vefa.peppol.common.model.TransportProfile;

import javax.inject.Singleton;
import java.util.List;

/**
 * Guice module orchestrating transmission related classes in transmission package.
 *
 * @author steinar
 *         Date: 18.11.2016
 *         Time: 16.10
 * @author erlend
 */
public class TransmissionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Transmitter.class).to(DefaultTransmitter.class).asEagerSingleton();

        bind(TransmissionRequestFactory.class).asEagerSingleton();

        bind(TransmissionService.class).to(DefaultTransmissionService.class).in(Singleton.class);

        bind(MessageSenderFactory.class).asEagerSingleton();
    }

    /**
     * Makes the prioritized list of supported transport profiles available to classes needing such a list (lookup...).
     *
     * @param messageSenderFactory Factory containing configuration for supported transport profiles.
     * @return Prioritized list of supported transport profiles.
     */
    @Provides
    @Singleton
    @Named("prioritized")
    List<TransportProfile> getTransportProfiles(MessageSenderFactory messageSenderFactory) {
        return messageSenderFactory.getPrioritizedTransportProfiles();
    }
}
