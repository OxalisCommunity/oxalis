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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.lang.OxalisPluginException;
import no.difi.oxalis.commons.filesystem.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * Implementation of {@link PluginFactory} making available type-specific objects for requested
 * classes implementing specific interfaces.
 *
 * @author steinar
 * @author erlend
 */
class PluginFactoryImpl implements PluginFactory {

    private ClassLoader classLoader;

    @Inject
    public PluginFactoryImpl(@Named("oxalis.ext.dir") Path endorsedDir) {
        if (!Files.isDirectory(endorsedDir) && Files.isReadable(endorsedDir)) {
            throw new OxalisPluginException(String.format("Unable to access directory '%s'.", endorsedDir));
        }

        classLoader = new URLClassLoader(
                findJarFiles(endorsedDir),
                Thread.currentThread().getContextClassLoader()
        );
    }

    /**
     * Receives a new instance of the implementation implementing the requested interface.
     *
     * @param cls Interface implemented by the implementation.
     * @param <T> Same as {@param cls}.
     * @return Initiated implementation of requested interface.
     */
    @Override
    public <T> T newInstance(Class<T> cls) {
        List<T> instances = Lists.newArrayList(ServiceLoader.load(cls, classLoader));

        if (instances.size() != 1)
            throw new OxalisPluginException(String.format("Found %s implementations of '%s'.",
                    instances.size(), cls.getCanonicalName()));

        return instances.get(0);
    }

    protected URL[] findJarFiles(Path endorsedDir) {
        String glob = "*.{jar}";

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(endorsedDir, glob)) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .map(FileUtils::toUrl)
                    .toArray(URL[]::new);
        } catch (IOException e) {
            throw new OxalisPluginException(
                    String.format("Error during list of '%s' files in '%s'.", glob, endorsedDir));
        }
    }
}
