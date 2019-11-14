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

package no.difi.oxalis.commons.config;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.commons.guice.OxalisModule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author erlend
 * @since 4.0.0
 */
@Slf4j
public class ConfigModule extends OxalisModule {

    @Override
    protected void configure() {
        Multibinder<PostConfig> multibinder = Multibinder.newSetBinder(binder(), PostConfig.class);
        multibinder.addBinding().to(JavaPropertiesPostConfig.class);
    }

    @Provides
    @Singleton
    @Named("file")
    protected Config loadConfigurationFile(@Named("conf") Path homePath) {
        Path configPath = homePath.resolve("oxalis.conf");
        log.info("Configuration file: {}", configPath);

        return Files.exists(configPath) && Files.isReadable(configPath) ?
                ConfigFactory.parseFile(configPath.toFile()).resolve() : 
                ConfigFactory.empty();
    }

    @Provides
    @Singleton
    @Named("reference")
    protected Config loadConfigurationReference() {
        Config referenceConfig = ConfigFactory.defaultReference();

        return referenceConfig
                .withFallback(referenceConfig.getConfig("defaults"));
    }

    @Provides
    @Singleton
    protected Config loadConfiguration(@Named("file") Config config, @Named("reference") Config referenceConfig,
                                       Set<PostConfig> postConfigs) {
        Config result = ConfigFactory.systemProperties()
                .withFallback(config)
                .withFallback(referenceConfig);

        // Performs actions when configuration is loaded.
        postConfigs.forEach(pc -> pc.perform(result));

        return result;
    }
}
