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

package no.difi.oxalis.ext.example;

import no.difi.oxalis.api.transmission.TransmissionVerifier;
import no.difi.oxalis.commons.guice.OxalisModule;
import org.slf4j.LoggerFactory;

/**
 * Guice module where we bind our implementations.
 *
 * @author erlend
 * @since 4.0.1
 */
public class ExampleModule extends OxalisModule {

    public ExampleModule() {
        LoggerFactory.getLogger(ExampleModule.class)
                .info("Loaded.");
    }

    @Override
    protected void configure() {
        bindTyped(TransmissionVerifier.class, LoggingTransmissionVerifier.class);
    }
}
