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

package no.difi.oxalis.commons.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import eu.peppol.lang.OxalisLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation for loading of Guice modules on same principles discussed on
 * <a href="http://stackoverflow.com/q/902639/135001">StackOverflow</a>, however this implementation uses
 * <a href="https://github.com/typesafehub/config">Typesafe Config</a> instead of Java ServiceLoader to allow for
 * further configuration than only "detected".
 *
 * @author erlend
 */
public class GuiceModuleLoader extends AbstractModule {

    private static Logger logger = LoggerFactory.getLogger(GuiceModuleLoader.class);

    private static String PREFIX = "oxalis.module";

    private static String CLS = "class";

    private static String ENABLED = "enabled";

    private static String OVERRIDE = "override";

    private static String DEPENDENCY = "dependency";

    public static Injector initiate() {
        return Guice.createInjector(getModules());
    }

    @Override
    protected void configure() {
        getModules().forEach(binder()::install);
    }

    protected static List<Module> getModules() {
        // Initial loading of configuration.
        Config config = ConfigFactory.load("oxalis");

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
            logger.debug("Loading module '{}' with override.", config.getString(CLS));
            return Modules.override(loadModule(config.getString(CLS)))
                    .with(loadModule(config.getString(OVERRIDE)));
        }

        // Loading without override.
        logger.debug("Loading module '{}'.", config.getString(CLS));
        return loadModule(config.getString(CLS));
    }

    protected static Module loadModule(String className) {
        try {
            return (Module) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new OxalisLoadingException(e.getMessage(), e);
        }
    }
}
