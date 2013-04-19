package eu.peppol.statistics.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.statistics.StatisticsRepository;
import eu.peppol.statistics.StatisticsRepositoryFactory;
import eu.peppol.statistics.StatisticsRepositoryFactoryProvider;

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
    StatisticsRepository provideStatisticsRepository() {
        // Retrieves an instance of the factory ....
        StatisticsRepositoryFactory statisticsRepositoryFactory = StatisticsRepositoryFactoryProvider.getInstance();
        // which, creates our repository instance
        return statisticsRepositoryFactory.getInstance();
    }

}
