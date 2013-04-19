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
    public void testGetInstance() throws Exception {

        OxalisDataSourceFactory oxalisDataSourceFactory = OxalisDataSourceFactoryProvider.getInstance();
        assertNotNull(oxalisDataSourceFactory);

        DataSource dataSource = oxalisDataSourceFactory.getDataSource();
        assertNotNull(dataSource);

        StatisticsRepositoryFactory statisticsRepositoryFactory = StatisticsRepositoryFactoryProvider.getInstance();
        assertNotNull(statisticsRepositoryFactory);

        StatisticsRepository statisticsRepository = statisticsRepositoryFactory.getInstance();
        assertNotNull(statisticsRepository);

        assertNotNull(statisticsRepository.getDataSource());
    }
}
