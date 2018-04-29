package no.difi.oxalis.commons.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import no.difi.oxalis.api.util.Type;
import no.difi.oxalis.commons.settings.SettingsBuilder;

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
