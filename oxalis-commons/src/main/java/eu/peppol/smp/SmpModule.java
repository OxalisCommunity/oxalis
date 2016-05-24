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

package eu.peppol.smp;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.peppol.util.GlobalConfiguration;

/**
 * @author steinar
 *         Date: 12.12.2015
 *         Time: 00.22
 */
public class SmpModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SmpContentRetriever.class).to(SmpContentRetrieverImpl.class);
        bind(BusDoxProtocolSelectionStrategy.class).to(DefaultBusDoxProtocolSelectionStrategyImpl.class);
    }

    @Provides
     SmpLookupManager provideSmpLookupManager(BusDoxProtocolSelectionStrategy busDoxProtocolSelectionStrategy, GlobalConfiguration globalConfiguration) {
        SmlHost smlHost = null;
        if (globalConfiguration.getSmlHostname() != null && globalConfiguration.getSmlHostname().trim().length() > 0) {
            String smlHostname = globalConfiguration.getSmlHostname();
            smlHost = new SmlHost(smlHostname);
        }

        return new SmpLookupManagerImpl(new SmpContentRetrieverImpl(), busDoxProtocolSelectionStrategy, globalConfiguration.getModeOfOperation(), smlHost);
    }

}
