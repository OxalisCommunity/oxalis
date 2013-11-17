package eu.peppol.persistence.sql;

import eu.peppol.statistics.*;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * 
 * @author steinar
 *         Date: 19.04.13
 *         Time: 16:37
 */
@Test(groups = "integration")
public class StatisticsRepositoryJdbcImplTest {

    private AggregatedStatisticsRepository repository;

    @BeforeTest
    public void createRepository() {
        StatisticsRepositoryFactory repositoryFactory = StatisticsRepositoryFactoryProvider.getInstance();
        repository = repositoryFactory.getInstanceForAggregatedStatistics();

    }
    @Test
    public void testPersist() throws Exception {

        Collection<AggregatedStatistics> aggregatedStatisticses = new AggregatedStatisticsSampleGenerator().generateEntries(100);
        assertEquals(aggregatedStatisticses.size(), 100);

        for (AggregatedStatistics aggregatedStatisticse : aggregatedStatisticses) {
            Integer persistId = repository.persist(aggregatedStatisticse);
        }

        Date start = new DateTime("2013-01-01T00").toDate();
        Date end = new Date();
    }
}
