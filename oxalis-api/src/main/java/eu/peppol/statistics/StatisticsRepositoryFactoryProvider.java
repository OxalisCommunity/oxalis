package eu.peppol.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Provides instances of the {@link StatisticsRepositoryFactory} by using the service located design pattern.
 * This implementation uses the typical Java idiom of META-INF/services.
 * <p/>
 * User: steinar
 * Date: 08.02.13
 * Time: 17:03
 */
public class StatisticsRepositoryFactoryProvider {

    public static final Logger log = LoggerFactory.getLogger(StatisticsRepositoryFactoryProvider.class);

    private static class StatisticsRepositoryFactoryHolder {
        private static final StatisticsRepositoryFactory INSTANCE = StatisticsRepositoryFactoryProvider.loadInstance();
    }

    /**
     * Provides a singleton instance of the StatisticsRepositoryFactory, which is lazy initialized.
     *
     * @return singleton instance of StatisticsRepositoryFactory
     *
     * @see #loadInstance() for details on how the factory is located.
     *
     */
    public static StatisticsRepositoryFactory getInstance() {
        return StatisticsRepositoryFactoryHolder.INSTANCE;
    }

    /**
     * Searches the class path for an implementation of StatisticsRepositoryFactory class.
     * This is a costly operation and should normally not be done more than once.
     *
     * @return
     *
     * @see #getInstance()
     */
    public static StatisticsRepositoryFactory loadInstance() {
        try {
            // Locates the implementation by locating and reading the contents of text file
            // META-INF/servces/eu.peppol.statistics.StatisticsRepositoryFactory
            ServiceLoader<StatisticsRepositoryFactory> serviceLoader = ServiceLoader.load(StatisticsRepositoryFactory.class);

            Iterator<StatisticsRepositoryFactory> iterator = serviceLoader.iterator();
            if (iterator.hasNext()) {

                // No support for multiple implementations, the first one is picked.
                StatisticsRepositoryFactory statisticsRepositoryFactory = iterator.next();
                if (statisticsRepositoryFactory != null) {
                    return statisticsRepositoryFactory;
                } else
                    throw new IllegalStateException("Unable to load implementation of " + StatisticsRepositoryFactory.class.getName() + " via META-INF/services");
            } else {
                throw new IllegalStateException("No implementation of " + StatisticsRepositoryFactory.class.getName() + " found in class path. \nVerify that oxalis-sql is in your class path");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load an implementation of " + StatisticsRepositoryFactory.class.getName() + "; " + e, e);
        }
    }
}
