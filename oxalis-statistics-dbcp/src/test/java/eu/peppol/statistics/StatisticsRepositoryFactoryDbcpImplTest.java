package eu.peppol.statistics;

/**
 * User: steinar
 * Date: 07.02.13
 * Time: 22:14
 */


import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Calendar;

/**
 * TODO: make this into an integration test
 */
public class StatisticsRepositoryFactoryDbcpImplTest {

    StatisticsRepositoryFactoryDbcpImpl statisticsRepositoryFactoryDbcp ;

    @BeforeTest
    public void setUp() {
        statisticsRepositoryFactoryDbcp = new StatisticsRepositoryFactoryDbcpImpl();
    }

    /**
     * Verifies that our DBCP stuff for configuration of a DataSource actually works.
     */
    @Test
    public void testSetupDataSourceFromGlobalConfiguration() throws SQLException {

        DataSource dataSource = statisticsRepositoryFactoryDbcp.setupDataSourceFromGlobalConfiguration();
        Connection connection = dataSource.getConnection();

        Statement statement = connection.createStatement();

        // This will probably only work with MySQL or any other database with same sql dialect.
        ResultSet resultSet = statement.executeQuery("select current_time");
        if (resultSet.next()) {
            Timestamp timestamp = resultSet.getTimestamp(1);
        }
    }

    @Test
    public void testInsertSampleData() {
        // Generates some sample data
        RawStatistics rawStatistics = RawStatisticsGenerator.sample();

        // Retrieves a repository object
        StatisticsRepository statisticsRepository = statisticsRepositoryFactoryDbcp.getInstance();

        // Persists raw statistics data
        statisticsRepository.persist(rawStatistics);
    }

}
