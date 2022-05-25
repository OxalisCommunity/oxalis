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

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import network.oxalis.api.lang.OxalisLoadingException;
import network.oxalis.api.settings.Path;
import network.oxalis.api.settings.Settings;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

/**
 * Helper class to allow for better information when invalid configuration is provided.
 *
 * @author erlend
 * @since 4.0.1
 */
public class ImplLoader {

    public static <T, C> T get(Injector injector, Class<T> cls, Settings<C> settings, C conf) {
        Key<T> key = Key.get(cls, settings.getNamed(conf));

        if (injector.getAllBindings().keySet().contains(key))
            return injector.getInstance(key);

        String available = injector.getAllBindings().keySet().stream()
                .filter(k -> k.getTypeLiteral().getRawType().equals(cls))
                .filter(k -> k.getAnnotation() != null)
                .map(Key::getAnnotation)
                .map(Named.class::cast)
                .map(Named::value)
                .collect(Collectors.joining(", "));

        try {
            Field field = conf.getClass().getField(((Enum) conf).name());

            throw new OxalisLoadingException(String.format(
                    "Implementation named '%s' for '%s' (%s) is not found. Available implementations: %s",
                    settings.getString(conf),
                    field.getAnnotation(Path.class).value(),
                    cls.getName(),
                    available
            ));
        } catch (NoSuchFieldException e) {
            throw new OxalisLoadingException(e.getMessage(), e);
        }
    }
}
