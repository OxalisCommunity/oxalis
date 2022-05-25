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

package network.oxalis.commons.plugin;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.plugin.PluginFactory;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.filesystem.ClassLoaderUtils;
import network.oxalis.commons.filesystem.FileSystemConf;
import network.oxalis.commons.guice.OxalisModule;

import java.nio.file.Path;

/**
 * This module enables extension of Oxalis using jar-files outside classpath.
 *
 * @author steinar
 * @author erlend
 * @since 4.0.0
 */
@Slf4j
public class PluginModule extends OxalisModule {

    @Override
    protected void configure() {
        bind(PluginFactory.class)
                .to(PluginFactoryImpl.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    @Named("plugin")
    public ClassLoader providesClassLoader(@Named("home") Path homeDirectory, Settings<FileSystemConf> settings) {
        final Path pluginPath = settings.getPath(FileSystemConf.PLUGIN, homeDirectory);
        log.info("Loading plugins from '{}'.", pluginPath);
        return ClassLoaderUtils.initiate(pluginPath);
    }
}
