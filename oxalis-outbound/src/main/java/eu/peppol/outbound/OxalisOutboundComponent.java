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

package eu.peppol.outbound;

import com.google.inject.Injector;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.transmission.TransmissionRequestFactory;
import no.difi.oxalis.api.config.GlobalConfiguration;
import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.oxalis.api.outbound.TransmissionService;
import no.difi.oxalis.api.outbound.Transmitter;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;

/**
 * Entry point and Object factory for the Oxalis outbound module.
 * <p>
 * Google guice is very lightweight, so there is really no need to make this a singleton in order to optimize for performance.
 *
 * @author steinar
 * @author thore
 * @author erlend
 */
public class OxalisOutboundComponent {

    private Injector injector = GuiceModuleLoader.initiate();

    /**
     * Retrieves instances of TransmissionRequestBuilder, while not exposing Google Guice to the outside
     *
     * @return instance of TransmissionRequestBuilder
     */
    public TransmissionRequestBuilder getTransmissionRequestBuilder() {
        return injector.getInstance(TransmissionRequestBuilder.class);
    }

    public TransmissionRequestFactory getTransmissionRequestFactory() {
        return injector.getInstance(TransmissionRequestFactory.class);
    }

    /**
     * Retrieves instance of LookupService, without revealing intern object dependency injection.
     */
    public LookupService getLookupService() {
        return injector.getInstance(LookupService.class);
    }

    /**
     * Retrieves instance of DefaultTransmitter, without revealing intern object dependency injection.
     *
     * @return instance of Transmitter
     */
    public Transmitter getTransmitter() {
        return injector.getInstance(Transmitter.class);
    }

    /**
     * Provides access to the Global configuration being used.
     *
     * @return
     */
    public GlobalConfiguration getGlobalConfiguration() {
        return injector.getInstance(GlobalConfiguration.class);
    }

    public EvidenceFactory getEvidenceFactory() {
        return injector.getInstance(EvidenceFactory.class);
    }

    /**
     * Provides access to the Google Guice injector in order to reuse the component with other components that also uses
     * Google Guice.
     *
     * @return
     */
    public Injector getInjector() {
        return injector;
    }

    public TransmissionService getTransmissionService() {
        return injector.getInstance(TransmissionService.class);
    }
}
