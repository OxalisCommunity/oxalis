package eu.peppol.jdbc;

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



    public static OxalisDataSourceFactory getInstance() {
        try {
            // Locates the implementation by locating and reading the contents of text file
            // META-INF/servces/eu.peppol.jdbc.OxalisDataSourceFactoryProvider
            ServiceLoader<OxalisDataSourceFactory> serviceLoader = ServiceLoader.load(OxalisDataSourceFactory.class);

            // No support for multiple implementations, the first one is picked.
            OxalisDataSourceFactory dataSourceFactory = serviceLoader.iterator().next();
            if (dataSourceFactory != null) {
                return dataSourceFactory;
            } else
                throw new IllegalStateException("Unable to load implementation of " + OxalisDataSourceFactory.class.getName() + " via META-INF/services");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load an implementation of " + OxalisDataSourceFactory.class.getName() + ". \nVerify that oxalis-statistics-jdbcp or oxalis-statistics-jndi is on your class path");
        }
    }
}
