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

package network.oxalis.commons.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import network.oxalis.api.util.Type;
import network.oxalis.commons.settings.SettingsBuilder;

/**
 * Specialized Guice module class with some extra methods heavily used by Oxalis.
 *
 * @author erlend
 * @since 4.0.1
 */
public abstract class OxalisModule extends AbstractModule {

    /**
     * Binds an implementation to an interface using the {@link Type} annotation as replacement
     * for {@link javax.inject.Named}.
     *
     * @param cls  Interface used for binding.
     * @param impl Implementation with {@link Type} annotation.
     */
    protected <T> void bindTyped(Class<T> cls, Class<? extends T> impl) {
        bindTyped(cls, impl, impl.getAnnotation(Type.class).value());
    }

    /**
     * Binds an implementation to an interface with names.
     *
     * @param cls   Interface used for binding.
     * @param impl  Implementation of interface..
     * @param names Names used to identify
     */
    protected <T> void bindTyped(Class<T> cls, Class<? extends T> impl, String... names) {
        for (String type : names)
            binder().skipSources(OxalisModule.class)
                    .bind(Key.get(cls, Names.named(type)))
                    .to(impl);
    }

    /**
     * Binds a configuration enum for use.
     *
     * @param cls Enum expressing configurations.
     */
    protected void bindSettings(Class<?> cls) {
        SettingsBuilder.with(binder(), cls);
    }
}
