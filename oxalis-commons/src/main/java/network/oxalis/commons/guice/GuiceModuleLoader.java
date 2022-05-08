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

package network.oxalis.commons.guice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.spi.Message;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.lang.OxalisLoadingException;
import network.oxalis.commons.config.ConfigModule;
import network.oxalis.commons.filesystem.FileSystemModule;

/**
 * Implementation for loading of Guice modules on same principles discussed on
 * <a href="http://stackoverflow.com/q/902639/135001">StackOverflow</a>, however this implementation uses
 * <a href="https://github.com/typesafehub/config">Typesafe Config</a> instead of Java ServiceLoader to allow for
 * further configuration than only "detected".
 *
 * @author erlend
 */
@Slf4j
public class GuiceModuleLoader extends AbstractModule {

    private static final String PREFIX = "oxalis.module";

    private static final String CLS = "class";

    private static final String ENABLED = "enabled";

    private static final String OVERRIDE = "override";

    private static final String DEPENDENCY = "dependency";

    public static Injector initiate(Module... modules) {
        List<Module> moduleList = new ArrayList<>();
        moduleList.addAll(getModules());
        moduleList.addAll(Arrays.asList(modules));

        try {
            return Guice.createInjector(moduleList);
        } catch (CreationException e) {
            if (e.getErrorMessages().stream()
                    .map(Message::getCause)
                    .allMatch(OxalisLoadingException.class::isInstance)) {
                e.getErrorMessages().stream()
                        .map(Message::getCause)
                        .filter(distinctByKey(Throwable::getMessage))
                        .forEach(c -> log.error(c.getMessage(), c));

                throw new OxalisLoadingException("Unable to load Oxalis due to errors during loading.");
            }

            throw e;
        }
    }

    @Override
    protected void configure() {
        getModules().forEach(binder()::install);
    }

    protected static List<Module> getModules() {
        // Initial loading of configuration.
        Injector initialInjector = Guice.createInjector(new FileSystemModule(), new ConfigModule());
        Config config = initialInjector.getInstance(Config.class);

        // List to gather configurations for modules.
        Map<String, Config> moduleConfigs = new HashMap<>();

        // Go through the two levels of identifiers for module configurations.
        for (String group : config.getObject(PREFIX).keySet()) {
            for (String module : config.getObject(String.format("%s.%s", PREFIX, group)).keySet()) {

                // Fetch configuration for the combination of group and module identifiers.
                Config moduleConfig = config.getConfig(String.format("%s.%s.%s", PREFIX, group, module));

                // Do not include disabled modules.
                if (!moduleConfig.hasPath(ENABLED) || moduleConfig.getBoolean(ENABLED))
                    moduleConfigs.put(String.format("%s.%s", group, module), moduleConfig);
            }
        }

        return moduleConfigs.values().stream()
                // Verify depending module is enabled.
                .filter(mc -> !mc.hasPath(DEPENDENCY) || moduleConfigs.containsKey(mc.getString(DEPENDENCY)))
                // Create Module instances from configuration.
                .map(GuiceModuleLoader::load)
                // Collect into list.
                .collect(Collectors.toList());
    }

    protected static Module load(Config config) {
        // Loading with override.
        if (config.hasPath(OVERRIDE)) {
            log.debug("Loading module '{}' with override.", config.getString(CLS));
            return Modules.override(loadModule(config.getString(CLS)))
                    .with(loadModule(config.getString(OVERRIDE)));
        }

        // Loading without override.
        log.debug("Loading module '{}'.", config.getString(CLS));
        return loadModule(config.getString(CLS));
    }

    protected static Module loadModule(String className) {
        try {
            return (Module) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new OxalisLoadingException(e.getMessage(), e);
        }
    }

    protected static <T, R> Predicate<T> distinctByKey(Function<? super T, R> function) {
        Set<R> set = new HashSet<>();
        return t -> set.add(function.apply(t));
    }
}
