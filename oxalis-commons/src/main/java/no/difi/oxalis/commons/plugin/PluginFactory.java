package no.difi.oxalis.commons.plugin;

/**
 * Interface describing a factory for creation of implementation of specific provided interface.
 *
 * @author erlend
 * @since 4.0.0
 */
public interface PluginFactory {

    <T> T newInstance(Class<T> cls);

}
