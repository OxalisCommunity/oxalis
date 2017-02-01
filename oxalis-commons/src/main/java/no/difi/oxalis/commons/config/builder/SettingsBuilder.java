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

package no.difi.oxalis.commons.config.builder;

import com.google.inject.*;
import com.google.inject.util.Types;
import com.typesafe.config.Config;
import no.difi.oxalis.api.config.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author erlend
 * @since 4.0.0
 */
public class SettingsBuilder<T> implements Provider<Settings<T>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsBuilder.class);

    private Config config;

    private final Map<T, String> settings = new HashMap<>();

    private final String title;

    @SuppressWarnings("unchecked")
    public static <T> SettingsBuilder<T> with(Binder binder, Class<T> cls, String title) {
        SettingsBuilder<T> settingsBuilder = new SettingsBuilder<>(title);

        binder.bind((Key<Settings<T>>) Key.get(Types.newParameterizedType(Settings.class, cls)))
                .toProvider(settingsBuilder)
                .in(Singleton.class);

        binder.requestInjection(settingsBuilder);

        return settingsBuilder;
    }

    private SettingsBuilder(String title) {
        this.title = title;
    }

    public SettingsBuilder add(T key, String path) {
        settings.put(key, path);
        return this;
    }

    @Inject
    public void setConfig(Config config) {
        this.config = config;

        LOGGER.info("Settings: {}", title);
        for (T key : settings.keySet())
            LOGGER.info("=> {}: {}", key, config.getString(settings.get(key)));
    }

    @Override
    public Settings<T> get() {
        return new TypesafeSettings<>(config, settings);
    }
}
