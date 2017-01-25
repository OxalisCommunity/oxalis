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

public class GuiceLoader {

    private static Logger logger = LoggerFactory.getLogger(GuiceLoader.class);

    private static String CLS = "class";

    private static String ENABLED = "enabled";

    private static String OVERRIDE = "override";

    private static String DEPENDENCY = "dependency";

    public static Injector initiate() {
        // Initial loading of configuration.
        Config config = ConfigFactory.load("oxalis");

        // List to gather configurations for modules.
        Map<String, Config> moduleConfigs = new HashMap<>();

        // Go through the two levels of identifiers for module configurations.
        for (String group : config.getObject("oxalis.module").keySet()) {
            for (String module : config.getObject(String.format("oxalis.module.%s", group)).keySet()) {

                // Fetch configuration for the combination of group and module identifiers.
                Config moduleConfig = config.getConfig(String.format("oxalis.module.%s.%s", group, module));

                // Do not include disabled modules.
                if (!moduleConfig.hasPath(ENABLED) || moduleConfig.getBoolean(ENABLED))
                    moduleConfigs.put(String.format("%s.%s", group, module), moduleConfig);
            }
        }

        List<Module> modules = moduleConfigs.values().stream()
                .filter(mc -> !mc.hasPath(DEPENDENCY) || moduleConfigs.containsKey(mc.getString(DEPENDENCY)))
                .peek(mc -> logger.info("Loading module '{}'.", mc.getString(CLS)))
                .map(GuiceLoader::load)
                .collect(Collectors.toList());

        return Guice.createInjector(modules);
    }

    protected static Module load(Config config) {
        if (config.hasPath(OVERRIDE))
            return Modules.override(loadModule(config.getString(CLS)))
                    .with(loadModule(config.getString(OVERRIDE)));

        return loadModule(config.getString(CLS));
    }

    @SuppressWarnings("unchecked")
    protected static Module loadModule(String className) {
        try {
            return (Module) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new OxalisLoadingException(e.getMessage(), e);
        }
    }
}
