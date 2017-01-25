/*
 * Copyright (c) 2010 - 2017 Norwegian Agency for Public Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package no.difi.oxalis.commons.plugin;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import no.difi.oxalis.api.persist.PayloadPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author steinar
 *         Date: 24.01.2017
 *         Time: 09.04
 */
public class PayloadPersisterProvider implements Provider<PayloadPersister> {

    public static final Logger log = LoggerFactory.getLogger(PayloadPersisterProvider.class);

    private final Path endorsedDir;

    private final PayloadPersister defaultPersister;

    @Inject
    public PayloadPersisterProvider(@Named("oxalis.ext.dir") Path endorsedDir,
                                    @Named("default") PayloadPersister defaultPersister) {
        this.endorsedDir = endorsedDir;
        if (endorsedDir == null) {
            throw new IllegalStateException("Must specify the Oxalis extension directory holding .jar files with plugins");
        }
        this.defaultPersister = defaultPersister;
        if (!Files.isDirectory(endorsedDir) && Files.isReadable(endorsedDir)) {
            throw new IllegalArgumentException("Unable to access directory " + endorsedDir);
        }
    }

    @Override
    public PayloadPersister get() {
        ClassLoader urlClassLoader = new URLClassLoader(findJarFiles(endorsedDir), Thread.currentThread().getContextClassLoader());

        List<PayloadPersister> persisters =
                Lists.newArrayList(ServiceLoader.load(PayloadPersister.class, urlClassLoader));

        if (persisters.isEmpty()) {
            log.info("No plugin implementations of {} found, reverting to default",
                    PayloadPersister.class.getCanonicalName());
            return defaultPersister;
        }
        if (persisters.size() > 1) {
            log.warn("Found {} implementations of {} returning first.",
                    persisters.size(), PayloadPersister.class.getCanonicalName());
        }
        return persisters.get(0);
    }

    protected URL[] findJarFiles(Path endorsedDir) {
        List<URL> urls = new ArrayList<>();
        String glob = "*.{jar}";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(endorsedDir, glob)) {
            stream.forEach(path -> {
                try {
                    URL url = path.toUri().toURL();
                    urls.add(url);
                } catch (MalformedURLException e) {
                    throw new IllegalStateException(path + " can not be converted to URL:" + e.getMessage(), e);
                }
            });

        } catch (IOException e) {
            throw new IllegalStateException("Error during list of " + glob + " files in " + endorsedDir);
        }

        return urls.toArray(new URL[urls.size()]);
    }
}


