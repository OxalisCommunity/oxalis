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

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import no.difi.oxalis.api.inbound.PayloadPersister;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author steinar
 *         Date: 24.01.2017
 *         Time: 10.43
 */
public class PluginModule extends AbstractModule {

    private final Path oxalisExtDirPath;

    public PluginModule() {
        // Load configuration properites from somewhere .....
        oxalisExtDirPath = Paths.get(System.getProperty("java.home"), "lib");
    }

    @Override
    protected void configure() {

        bind(PayloadPersister.class).toInstance((mi, h, im) -> null);   // Makes this class available to the PayloadPersisterProvider

        // This path is guaranteed to exist on every machine, hence the test should not fail.
        // the path is merely for testing, must be replaced with something meaningfull
        bind(Path.class).annotatedWith(Names.named("oxalis.ext.dir")).toInstance(oxalisExtDirPath);
        bind(PayloadPersister.class).toProvider(PayloadPersisterProvider.class);
    }
}
