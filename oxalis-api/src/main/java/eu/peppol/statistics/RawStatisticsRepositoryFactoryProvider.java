package eu.peppol.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Provides instances of the {@link RawStatisticsRepositoryFactory} by using the service located design pattern.
 * This implementation uses the typical Java idiom of META-INF/services.
 * <p/>
 * @author steinar
 * @author thore
 */
public class RawStatisticsRepositoryFactoryProvider {

    public static final Logger log = LoggerFactory.getLogger(RawStatisticsRepositoryFactoryProvider.class);

    private static class StatisticsRepositoryFactoryHolder {
        private static final RawStatisticsRepositoryFactory INSTANCE = RawStatisticsRepositoryFactoryProvider.loadInstance();
    }

    /**
     * Provides a singleton instance of the StatisticsRepositoryFactory, which is lazy initialized.
     *
     * @return singleton instance of StatisticsRepositoryFactory
     *
     * @see #loadInstance() for details on how the factory is located.
     *
     */
    public static RawStatisticsRepositoryFactory getInstance() {
        return StatisticsRepositoryFactoryHolder.INSTANCE;
    }

    /**
     * Searches the class path for an implementation of StatisticsRepositoryFactory class.
     * This is a costly operation and should normally not be done more than once.
     *
     * @return returns the first implementation that is not built-in (or the built-in if no others are found)
     *
     * @see #getInstance()
     */
    public static RawStatisticsRepositoryFactory loadInstance() {
        try {
            log.info("Searching the class path for instances of " + RawStatisticsRepositoryFactory.class.getSimpleName());
            // Locates the implementation by locating and reading the contents of text file
            // META-INF/services/eu.peppol.statistics.StatisticsRepositoryFactory
            ServiceLoader<RawStatisticsRepositoryFactory> serviceLoader = ServiceLoader.load(RawStatisticsRepositoryFactory.class);
            Iterator<RawStatisticsRepositoryFactory> iterator = serviceLoader.iterator();
            if (iterator.hasNext()) {
                RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory = iterator.next();
                if (rawStatisticsRepositoryFactory != null) {
                    log.info("Found a {} implementation in {}", RawStatisticsRepositoryFactory.class.getSimpleName(), rawStatisticsRepositoryFactory.getClass().getName());
                    if (rawStatisticsRepositoryFactory.getClass().getName().equals("eu.peppol.persistence.sql.RawStatisticsRepositoryFactoryJdbcImpl")) {
                        log.info("Since {} is the built-in implementation, we will look for other 3rd party implementaions", rawStatisticsRepositoryFactory.getClass().getSimpleName());
                        if (iterator.hasNext()) {
                            rawStatisticsRepositoryFactory = iterator.next();
                            if (rawStatisticsRepositoryFactory != null) {
                                log.info("Found another {} implementation in {}, using that instead", RawStatisticsRepositoryFactory.class.getSimpleName(), rawStatisticsRepositoryFactory.getClass().getName());
                            }
                        }
                    }
                }
                // ... we still haven't found what we're looking for ...
                if (rawStatisticsRepositoryFactory == null) {
                    throw new IllegalStateException("Unable to load implementation of " + RawStatisticsRepositoryFactory.class.getSimpleName() + " via META-INF/services");
                }
                return rawStatisticsRepositoryFactory;
            } else {
                throw new IllegalStateException("No implementation of " + RawStatisticsRepositoryFactory.class.getSimpleName() + " found in class path.\nVerify that oxalis-sql module is in your class path (it contains the dafault implementation)");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load an implementation of " + RawStatisticsRepositoryFactory.class.getName() + "; " + e, e);
        }
    }

}
