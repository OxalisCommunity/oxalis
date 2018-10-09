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

package no.difi.oxalis.commons.filesystem;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.commons.guice.OxalisModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author erlend
 * @since 4.0.0
 */
public class FileSystemModule extends OxalisModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemModule.class);

    @Override
    protected void configure() {
        bindSettings(FileSystemConf.class);
    }

    @Provides
    @Singleton
    @Named("home")
    protected Path getHomeFolder(OxalisHomeDirectory oxalisHomeDirectory) {
        Path path = oxalisHomeDirectory.detect().toPath();
        LOGGER.info("Home folder: {}", path);
        return path;
    }

    @Provides
    @Singleton
    @Named("conf")
    protected Path getConfFolder(@Named("reference") Config referenceConfig, @Named("home") Path homeFolder) {
        Config config = ConfigFactory.systemProperties()
                .withFallback(referenceConfig);

        Path path = homeFolder;

        if (config.hasPath("oxalis.path.conf"))
            path = homeFolder.resolve(config.getString("oxalis.path.conf"));

        LOGGER.info("Configuration folder: {}", path);
        return path;
    }

    @Provides
    @Singleton
    @Named("inbound")
    protected Path getInboundFolder(Settings<FileSystemConf> settings, @Named("home") Path homeFolder) {
        Path path = settings.getPath(FileSystemConf.INBOUND, homeFolder);
        LOGGER.info("Inbound folder: {}", path);
        return path;
    }

    @Provides
    @Named("environment")
    protected Map<String, String> getSystemEnvironment() {
        return System.getenv();
    }
}
