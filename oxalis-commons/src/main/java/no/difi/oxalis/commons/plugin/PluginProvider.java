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
import eu.peppol.lang.OxalisPluginException;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author steinar
 *         Date: 24.01.2017
 *         Time: 09.04
 * @author erlend
 */
class PluginProvider<T> implements Provider<T> {

    private final ClassLoader classLoader;

    private final Class<T> cls;

    @Inject
    public PluginProvider(ClassLoader classLoader, Class<T> cls) {
        this.classLoader = classLoader;
        this.cls = cls;
    }

    @Override
    public T get() {
        List<T> instances = Lists.newArrayList(ServiceLoader.load(cls, classLoader));

        if (instances.isEmpty())
            throw new OxalisPluginException(String.format("No plugin implementations of '%s' found.",
                    cls.getCanonicalName()));

        if (instances.size() > 1)
            throw new OxalisPluginException(String.format("Found %s implementations of '%s'.",
                    instances.size(), cls.getCanonicalName()));

        return instances.get(0);
    }
}


