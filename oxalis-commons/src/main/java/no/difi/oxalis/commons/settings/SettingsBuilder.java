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

package no.difi.oxalis.commons.settings;

import com.google.inject.*;
import com.google.inject.util.Types;
import com.typesafe.config.Config;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Title;
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
    public static <T> SettingsBuilder<T> with(Binder binder, Class<T> cls) {
        SettingsBuilder<T> settingsBuilder = new SettingsBuilder<>(cls.getAnnotation(Title.class).value());

        binder.bind((Key<Settings<T>>) Key.get(Types.newParameterizedType(Settings.class, cls)))
                .toProvider(settingsBuilder)
                .in(Singleton.class);

        binder.requestInjection(settingsBuilder);

        for (T t : cls.getEnumConstants())
            settingsBuilder.add(t);

        return settingsBuilder;
    }

    private SettingsBuilder(String title) {
        this.title = title;
    }

    private SettingsBuilder<T> add(T key) {
        settings.put(key, TypesafeSettings.getField(key).getAnnotation(Path.class).value());
        return this;
    }

    @Inject
    public void setConfig(Config config) {
        this.config = config;

        Settings<T> result = get();

        LOGGER.info("Settings: {}", title);
        settings.keySet().stream()
                .sorted()
                .forEach(key -> LOGGER.info("=> {}: {}",
                        key, result.getString(key)));
    }

    @Override
    public Settings<T> get() {
        return new TypesafeSettings<>(config, settings);
    }
}
