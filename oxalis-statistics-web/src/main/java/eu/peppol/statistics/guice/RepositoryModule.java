package eu.peppol.statistics.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;
import eu.peppol.statistics.RawStatisticsRepositoryFactoryProvider;

/**
 * @author steinar
 *         Date: 18.04.13
 *         Time: 11:12
 */
public class RepositoryModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides @Singleton
    RawStatisticsRepository provideStatisticsRepository() {
        // Retrieves an instance of the factory ....
        RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory = RawStatisticsRepositoryFactoryProvider.getInstance();
        // which, creates our repository instance
        return rawStatisticsRepositoryFactory.getInstanceForRawStatistics();
    }

}
