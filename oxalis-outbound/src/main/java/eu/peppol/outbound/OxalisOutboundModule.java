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

package eu.peppol.outbound;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.transmission.Transmitter;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.smp.SmpModule;
import eu.peppol.util.OxalisCommonsModule;

/**
 * Object factory for the Oxalis outbound module.
 *
 * Google guice is very lightweight, so there is really no need to make this a singleton in order to optimize for performance.
 *
 * @author steinar
 * @author thore
 */
public class OxalisOutboundModule {

    Injector injector;

    public OxalisOutboundModule() {
        injector = Guice.createInjector(
                new OxalisCommonsModule(),
                new SmpModule()
        );
    }

    /**
     * Retrieves instances of TransmissionRequestBuilder, while not exposing Google Guice to the outside
     *
     * @return instance of TransmissionRequestBuilder
     */
    public TransmissionRequestBuilder getTransmissionRequestBuilder() {
        return injector.getInstance(TransmissionRequestBuilder.class);
    }

    /**
     * Retrieves instance of Transmitter, without revealing intern object dependency injection.
     *
     * @return instance of Transmitter
     */
    public Transmitter getTransmitter() {
        return injector.getInstance(Transmitter.class);
    }

    /**
     * Retrieves instance of SmpLookupManager, without revealing intern object dependency injection.
     */
    public SmpLookupManager getSmpLookupManager() {
        return injector.getInstance(SmpLookupManager.class);
    }

}