package no.difi.oxalis.commons.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import no.difi.oxalis.api.lang.OxalisLoadingException;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Settings;

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
