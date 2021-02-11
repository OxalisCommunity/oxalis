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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import network.oxalis.api.lang.OxalisPluginException;
import network.oxalis.api.plugin.PluginFactory;
import network.oxalis.commons.guice.GuiceServiceLoader;

import java.util.List;

/**
 * Implementation of {@link PluginFactory} making available type-specific objects for requested
 * classes implementing specific interfaces.
 *
 * @author steinar
 * @author erlend
 * @since 4.0.0
 */
class PluginFactoryImpl implements PluginFactory {

    private final GuiceServiceLoader guiceServiceLoader;

    private final ClassLoader classLoader;

    @Inject
    public PluginFactoryImpl(GuiceServiceLoader guiceServiceLoader, @Named("plugin") ClassLoader classLoader) {
        this.guiceServiceLoader = guiceServiceLoader;
        this.classLoader = classLoader;
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
        List<T> instances = guiceServiceLoader.load(cls, classLoader);

        if (instances.size() != 1)
            throw new OxalisPluginException(String.format("Found %s implementations of '%s'.",
                    instances.size(), cls.getCanonicalName()));

        return instances.get(0);
    }
}
