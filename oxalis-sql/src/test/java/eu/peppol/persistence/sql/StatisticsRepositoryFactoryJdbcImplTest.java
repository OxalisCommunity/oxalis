package eu.peppol.persistence.sql;

import eu.peppol.jdbc.OxalisDataSourceFactory;
import eu.peppol.jdbc.OxalisDataSourceFactoryProvider;
import eu.peppol.statistics.StatisticsRepository;
import eu.peppol.statistics.StatisticsRepositoryFactory;
import eu.peppol.statistics.StatisticsRepositoryFactoryProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 18.04.13
 *         Time: 16:08
 */

@Test(groups = "integration")
public class StatisticsRepositoryFactoryJdbcImplTest {

    @Test
    public void oxalisDataSourceFactoryIsSingleton() throws Exception {

        // Attempts to load the first instance of OxalisDataSourceFactory
        OxalisDataSourceFactory oxalisDataSourceFactory = OxalisDataSourceFactoryProvider.getInstance();
        assertNotNull(oxalisDataSourceFactory);

        // Second invocation should return same instance
        OxalisDataSourceFactory oxalisDataSourceFactory2 = OxalisDataSourceFactoryProvider.getInstance();

        assertEquals(oxalisDataSourceFactory, oxalisDataSourceFactory2,"Seems the Singletong pattern in OxalisDataSourceFactoryProvider is not working");


        DataSource dataSource1 = oxalisDataSourceFactory.getDataSource();
        assertNotNull(dataSource1);

        DataSource dataSource2 = oxalisDataSourceFactory.getDataSource();

        assertEquals(dataSource1, dataSource2, OxalisDataSourceFactory.class.getSimpleName() + " is not returning a singleton instance of DataSource");

    }


    @Test
    public void statisticsRepositoryFactoryIsSingleton() {
        // Verifies that the StatisticsRepositoryFactoryProvider returns a singleton instance
        StatisticsRepositoryFactory statisticsRepositoryFactory = StatisticsRepositoryFactoryProvider.getInstance();
        assertNotNull(statisticsRepositoryFactory);
        StatisticsRepositoryFactory statisticsRepositoryFactory2 = StatisticsRepositoryFactoryProvider.getInstance();
        assertEquals(statisticsRepositoryFactory, statisticsRepositoryFactory2,"Singleton pattern brok in " + StatisticsRepositoryFactoryProvider.class.getSimpleName());


        // However; the StatisticsRepository instances should not be singleton
        StatisticsRepository statisticsRepository = statisticsRepositoryFactory.getInstance();
        assertNotNull(statisticsRepository);

        assertNotNull(statisticsRepository.getDataSource());

        StatisticsRepository statisticsRepository2 = statisticsRepositoryFactory.getInstance();
        assertNotEquals(statisticsRepository, statisticsRepository2,"StatisticsRepositoryFactory.getInstance() should not produce singleton instance");

    }
}
