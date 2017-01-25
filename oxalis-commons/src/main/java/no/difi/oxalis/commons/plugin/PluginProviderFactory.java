package no.difi.oxalis.commons.plugin;

import com.google.inject.Provider;

/**
 * @author erlend
 * @since 4.0.0
 */
public interface PluginProviderFactory {

    <T> Provider<T> newProvider(Class<T> cls);

}
