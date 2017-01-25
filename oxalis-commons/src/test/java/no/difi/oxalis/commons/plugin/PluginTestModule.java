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

import com.google.inject.Inject;
import eu.peppol.lang.OxalisPluginException;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.commons.guice.TestOxalisKeystoreModule;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.persist.PersisterModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

/**
 * @author steinar
 *         Date: 24.01.2017
 *         Time: 10.45
 * @author erlend
 */
@Guice(modules = {PluginModule.class, ModeModule.class, PersisterModule.class, TestOxalisKeystoreModule.class})
public class PluginTestModule {

    @Inject
    private PluginProviderFactory pluginProviderFactory;

    @Test(expectedExceptions = OxalisPluginException.class)
    public void pluginNotFound() throws Exception {
        pluginProviderFactory.newProvider(PayloadPersister.class).get();
    }
}