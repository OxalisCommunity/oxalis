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

package network.oxalis.commons.timestamp;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import network.oxalis.api.settings.Settings;
import network.oxalis.api.timestamp.TimestampProvider;
import network.oxalis.commons.guice.ImplLoader;
import network.oxalis.commons.guice.OxalisModule;

/**
 * Guice module making a default implementation of {@link TimestampProvider} available.
 * <p>
 * Available services (timestamp.service):
 * <ul>
 * <li>system</li>
 * </ul>
 *
 * @author erlend
 * @since 4.0.0
 */
public class TimestampModule extends OxalisModule {

    @Override
    protected void configure() {
        bindTyped(TimestampProvider.class, SystemTimestampProvider.class);

        bindSettings(TimestampConf.class);
    }

    @Provides
    @Singleton
    protected TimestampProvider getTimestampProvider(Injector injector, Settings<TimestampConf> settings) {
        return ImplLoader.get(injector, TimestampProvider.class, settings, TimestampConf.SERVICE);
    }
}
