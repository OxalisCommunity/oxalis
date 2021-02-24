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

package network.oxalis.commons.logging;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import network.oxalis.api.logging.Configurator;
import network.oxalis.commons.util.ClassUtils;
import network.oxalis.commons.guice.OxalisModule;

/**
 * @author erlend
 */
public class LoggingModule extends OxalisModule {

    @Override
    protected void configure() {
        bindSettings(LoggingConf.class);

        binder().requestInjection(new LoggingHandler());
    }

    @Provides
    @Named("logback")
    @SuppressWarnings("unchecked")
    protected Configurator provideLogbackConfigurator(Injector injector) {
        // Loads class using string to make sure the class is not automatically loaded.
        return injector.getInstance((Class<Configurator>)
                ClassUtils.load("network.oxalis.commons.logging.LogbackConfigurator"));
    }
}
