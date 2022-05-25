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

package network.oxalis.outbound;

import com.google.inject.Injector;
import network.oxalis.api.evidence.EvidenceFactory;
import network.oxalis.api.lookup.LookupService;
import network.oxalis.api.outbound.TransmissionService;
import network.oxalis.api.outbound.Transmitter;
import network.oxalis.commons.guice.GuiceModuleLoader;
import network.oxalis.outbound.transmission.TransmissionRequestBuilder;
import network.oxalis.outbound.transmission.TransmissionRequestFactory;

/**
 * Entry point and Object factory for the Oxalis outbound module.
 *
 * Should be treated as a singleton when used to make sure Oxalis is not loaded more times than needed.
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
