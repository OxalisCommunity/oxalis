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

package network.oxalis.commons.settings;

import com.google.inject.*;
import com.google.inject.util.Types;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.settings.Path;
import network.oxalis.api.settings.Settings;
import network.oxalis.api.settings.Title;
import network.oxalis.commons.persist.PersisterConf;
import network.oxalis.commons.guice.OxalisModule;

import java.util.HashMap;
import java.util.Map;

/**
 * Binds the parameterized configuration values described in annotations on enums to the configuration values
 * found in the supplied type safe config.
 *
 * @author erlend
 * @since 4.0.0
 */
@Slf4j
public class SettingsBuilder<T> implements Provider<Settings<T>> {

    private Config config;

    private final Map<T, String> settings = new HashMap<>();

    private final String title;

    /**
     * Binds the annotations of the supplied configuration enum as guice names with the values obtained from
     * typesafe config.
     *
     * @param binder current Guice binder
     * @param cls    the enum class with annotations
     * @param <T>    the type literal of the enum for instance {@link PersisterConf}
     * @return instance of SettingsBuilder
     */
    @SuppressWarnings("unchecked")
    public static <T> SettingsBuilder<T> with(Binder binder, Class<T> cls) {

        // Grabs the value of the @Title annotation and creates instance of SettingsBuilder of the enum
        SettingsBuilder<T> settingsBuilder = new SettingsBuilder<>(cls.getAnnotation(Title.class).value());

        binder.skipSources(SettingsBuilder.class, OxalisModule.class)
                .bind((Key<Settings<T>>) Key.get(Types.newParameterizedType(Settings.class, cls)))
                .toProvider(settingsBuilder)
                .in(Singleton.class);

        binder.skipSources(SettingsBuilder.class, OxalisModule.class).requestInjection(settingsBuilder);

        for (T t : cls.getEnumConstants())
            settingsBuilder.add(t);

        return settingsBuilder;
    }

    private SettingsBuilder(String title) {
        this.title = title;
    }

    private void add(T key) {
        settings.put(key, TypesafeSettings.getField(key).getAnnotation(Path.class).value());
    }

    @Inject
    public void setConfig(Config config) {
        this.config = config;

        Settings<T> result = get();

        settings.keySet().stream()
                .sorted()
                .forEach(key -> log.info("{} => {}: {}",
                        title, key, result.toLogSafeString(key)));
    }

    @Override
    public Settings<T> get() {
        return new TypesafeSettings<>(config, settings);
    }
}
