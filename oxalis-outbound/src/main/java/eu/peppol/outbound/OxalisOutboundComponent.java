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
import eu.peppol.tracing.TracingModule;
import eu.peppol.outbound.module.LookupModule;
import eu.peppol.outbound.transmission.TransmissionRequestFactory;
import no.difi.oxalis.commons.module.ModeModule;
import eu.peppol.outbound.module.TransmissionModule;
import eu.peppol.outbound.transmission.SimpleTransmitter;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.api.Transmitter;
import eu.peppol.persistence.guice.OxalisDataSourceModule;
import eu.peppol.persistence.guice.RepositoryModule;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.smp.SmpModule;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OxalisKeystoreModule;
import eu.peppol.util.OxalisProductionConfigurationModule;

/**
 * Entry point and Object factory for the Oxalis outbound module.
 * <p>
 * <p>
 * Google guice is very lightweight, so there is really no need to make this a singleton in order to optimize for performance.
 *
 * @author steinar
 * @author thore
 */
public class OxalisOutboundComponent {

    Injector injector;

    public OxalisOutboundComponent() {
        injector = Guice.createInjector(
                new OxalisProductionConfigurationModule(),
                new OxalisKeystoreModule(),
                new TracingModule(),
                new ModeModule(),
                new LookupModule(),
                new OxalisDataSourceModule(),
                new RepositoryModule(),
                new SmpModule(),
                new TransmissionModule()
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

    public TransmissionRequestFactory getTransmissionRequestFactory() {
        return injector.getInstance(TransmissionRequestFactory.class);
    }

    /**
     * Retrieves instance of SmpLookupManager, without revealing intern object dependency injection.
     */
    public SmpLookupManager getSmpLookupManager() {
        return injector.getInstance(SmpLookupManager.class);
    }

    /**
     * Retrieves instance of SimpleTransmitter, without revealing intern object dependency injection.
     *
     * @return instance of Transmitter
     */
    public Transmitter getSimpleTransmitter() {
        return injector.getInstance(SimpleTransmitter.class);
    }


    public Transmitter getEvidencePersistingTransmitter() {
        return null;
    }

    /**
     * Provides access to the Global configuration being used.
     *
     * @return
     */
    public GlobalConfiguration getGlobalConfiguration() {
        return injector.getInstance(GlobalConfiguration.class);
    }

    /**
     * Provides access to the Google Guice injector in order to reuse the component with other components that also uses
     * Google Guice.
     * @return
     */
    public Injector getInjector() {
        return injector;
    }
}