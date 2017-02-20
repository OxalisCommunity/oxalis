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

package no.difi.oxalis.commons.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.commons.filesystem.ClassLoaderUtils;
import no.difi.oxalis.commons.filesystem.FileSystemConf;

import java.nio.file.Path;

/**
 * This module enables extension of Oxalis using jar-files outside classpath.
 *
 * @author steinar
 * @author erlend
 * @since 4.0.0
 */
public class PluginModule extends AbstractModule {

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
        return ClassLoaderUtils.initiate(pluginPath);
    }
}
