package eu.peppol.persistence.sql;

import eu.peppol.jdbc.OxalisDataSourceFactory;
import eu.peppol.jdbc.OxalisDataSourceFactoryProvider;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;
import eu.peppol.statistics.RawStatisticsRepositoryFactoryProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 18.04.13
 *         Time: 16:08
 */

@Test(groups = "integration")
public class RawStatisticsRepositoryFactoryJdbcImplTest {

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
        RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory = RawStatisticsRepositoryFactoryProvider.getInstance();
        assertNotNull(rawStatisticsRepositoryFactory);
        RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory2 = RawStatisticsRepositoryFactoryProvider.getInstance();
        assertEquals(rawStatisticsRepositoryFactory, rawStatisticsRepositoryFactory2,"Singleton pattern brok in " + RawStatisticsRepositoryFactoryProvider.class.getSimpleName());


        // However; the StatisticsRepository instances should not be singleton
        RawStatisticsRepository rawStatisticsRepository = rawStatisticsRepositoryFactory.getInstanceForRawStatistics();
        assertNotNull(rawStatisticsRepository);

        RawStatisticsRepository rawStatisticsRepository2 = rawStatisticsRepositoryFactory.getInstanceForRawStatistics();
        assertNotEquals(rawStatisticsRepository, rawStatisticsRepository2,"StatisticsRepositoryFactory.getInstanceForRawStatistics() should not produce singleton instance");

    }
}
