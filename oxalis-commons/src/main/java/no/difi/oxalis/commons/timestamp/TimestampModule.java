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

package no.difi.oxalis.commons.timestamp;

import com.google.inject.*;
import com.google.inject.name.Names;
import no.difi.oxalis.api.config.Settings;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.commons.config.builder.SettingsBuilder;

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
public class TimestampModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(TimestampProvider.class, Names.named("system")))
                .to(SystemTimestampProvider.class)
                .in(Singleton.class);

        SettingsBuilder.with(binder(), TimestampConf.class, "Timestamp")
                .add(TimestampConf.SERVICE, "timestamp.service");
    }

    @Provides
    @Singleton
    protected TimestampProvider getTimestampService(Injector injector, Settings<TimestampConf> settings) {
        return injector.getInstance(
                // Key.get(TimestampProvider.class, Names.named(config.getString("timestamp.service"))));
                Key.get(TimestampProvider.class, settings.getNamed(TimestampConf.SERVICE)));
    }
}
