package eu.peppol.jdbc;

import eu.peppol.statistics.StatisticsRepositoryFactory;

import java.util.ServiceLoader;

/**
 * Provides instances of the {@link eu.peppol.statistics.StatisticsRepositoryFactory} by using the service located design pattern.
 * This implementation uses the typical Java idiom of META-INF/services.
 *
 * User: steinar
 * Date: 08.02.13
 * Time: 17:03
 */
public class OxalisDataSourceFactoryProvider {

    public static StatisticsRepositoryFactory getInstance() {
        try {
            // Locates the implementation by locating and reading the contents of text file
            // META-INF/servces/eu.peppol.statistics.StatisticsRepositoryFactory
            ServiceLoader<StatisticsRepositoryFactory> serviceLoader = ServiceLoader.load(StatisticsRepositoryFactory.class);

            // No support for multiple implementations, the first one is picked.
            StatisticsRepositoryFactory statisticsRepositoryFactory = serviceLoader.iterator().next();
            if (statisticsRepositoryFactory != null) {
                return statisticsRepositoryFactory;
            } else
                throw new IllegalStateException("Unable to load implementation of " + StatisticsRepositoryFactory.class.getName() + " via META-INF/services");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load an implementation of " + StatisticsRepositoryFactory.class.getName() + ". \nVerify that oxalis-statistics-jdbcp or oxalis-statistics-jndi is on your class path");
        }
    }
}
