package eu.peppol.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * Provides instances of the {@link OxalisDataSourceFactoryProvider} by using the service located design pattern.
 * This implementation uses the typical Java idiom of META-INF/services.
 *
 * User: steinar
 * Date: 08.02.13
 * Time: 17:03
 */
public class OxalisDataSourceFactoryProvider {

    private static final Logger log = LoggerFactory.getLogger(OxalisDataSourceFactoryProvider.class);

    private static class OxalisDataSourceFactoryHolder {
        private static final OxalisDataSourceFactory INSTANCE = OxalisDataSourceFactoryProvider.loadInstance();
    }


    /**
     * Singleton, lazy loaded, thread safe implementation, i.e. will always return the same OxalisDataSourceFactory
     *
     * @return
     */
    public static OxalisDataSourceFactory getInstance() {
        return OxalisDataSourceFactoryHolder.INSTANCE;
    }

    /**
     * Locates implementations of the OxalisDataSourceFactory using the META-INF/services idiom.
     * This method requires a little heavy lifting.
     *
     * @return a new instance of the OxalisDataSourceFactory
     * @see #getInstance() for an optimal solution in terms of performance.
     */
    public static OxalisDataSourceFactory loadInstance() {
        log.debug("Loading instance of " + OxalisDataSourceFactory.class.getName() + " from class path using META-INF/services idiom");

        try {
            // Locates the implementation by locating and reading the contents of text file
            // META-INF/servces/eu.peppol.jdbc.OxalisDataSourceFactory
            ServiceLoader<OxalisDataSourceFactory> serviceLoader = ServiceLoader.load(OxalisDataSourceFactory.class);

            // No support for multiple implementations, the first one is picked.
            OxalisDataSourceFactory dataSourceFactory = serviceLoader.iterator().next();
            if (dataSourceFactory != null) {
                return dataSourceFactory;
            } else
                throw new IllegalStateException("Unable to load implementation of " + OxalisDataSourceFactory.class.getName() + " via META-INF/services");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load an implementation of " + OxalisDataSourceFactory.class.getName() + ". \nVerify that oxalis-jdbc-dbcp or oxalis-jdbc-jndi is on your class path");
        }
    }
}
