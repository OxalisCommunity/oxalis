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

package network.oxalis.outbound.transmission;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import network.oxalis.api.outbound.TransmissionService;
import network.oxalis.api.outbound.Transmitter;
import network.oxalis.api.transformer.ContentWrapper;
import network.oxalis.commons.guice.OxalisModule;
import network.oxalis.outbound.transformer.XmlContentWrapper;
import network.oxalis.vefa.peppol.common.model.TransportProfile;

import javax.inject.Singleton;
import java.util.List;

/**
 * Guice module orchestrating transmission related classes in transmission package.
 *
 * @author steinar
 * Date: 18.11.2016
 * Time: 16.10
 * @author erlend
 */
public class TransmissionModule extends OxalisModule {

    @Override
    protected void configure() {
        bind(Transmitter.class)
                .to(DefaultTransmitter.class)
                .asEagerSingleton();

        bind(TransmissionRequestFactory.class)
                .asEagerSingleton();

        bind(TransmissionService.class)
                .to(DefaultTransmissionService.class);

        bind(MessageSenderFactory.class)
                .asEagerSingleton();

        bindTyped(ContentWrapper.class, XmlContentWrapper.class);
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
    protected List<TransportProfile> getTransportProfiles(MessageSenderFactory messageSenderFactory) {
        return messageSenderFactory.getPrioritizedTransportProfiles();
    }
}
