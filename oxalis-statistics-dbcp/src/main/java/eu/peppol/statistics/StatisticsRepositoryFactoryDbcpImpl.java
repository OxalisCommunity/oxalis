package eu.peppol.statistics;

import eu.peppol.util.GlobalConfiguration;
import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;

import javax.sql.DataSource;

/**
 * Provides StatisticsRepository objects, ready to use with a DataSource injected etc.
 *
 * This implementation will use DBCP to create a connection pool.
 *
 * User: steinar
 * Date: 07.02.13
 * Time: 22:17
 */
public class StatisticsRepositoryFactoryDbcpImpl implements StatisticsRepositoryFactory {

    DataSource dataSource;
    private final GlobalConfiguration globalConfiguration;

    public StatisticsRepositoryFactoryDbcpImpl() {
        this.globalConfiguration = GlobalConfiguration.getInstance();
    }

    @Override
    public StatisticsRepository getInstance() {
        return new StatisticsRepositoryJdbcImpl(getDataSource());
    }

    private synchronized DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = setupDataSourceFromGlobalConfiguration();
        }

        return dataSource;
    }


    DataSource setupDataSourceFromGlobalConfiguration() {
        String className = globalConfiguration.getJdbcDriverClassName();
        String connectURI = globalConfiguration.getConnectionURI();
        String userName = globalConfiguration.getUserName();
        String password = globalConfiguration.getPassword();

        DataSource ds = createDataSource(className, connectURI, userName, password);
        return ds;

    }


    DataSource createDataSource(String className, String connectURI, String username, String password) {
        DriverAdapterCPDS cpds = new DriverAdapterCPDS();
        try {
            cpds.setDriver(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load the JDBC driver " + className + ". Check your class path.");
        }
        cpds.setUrl(connectURI);
        cpds.setUser(username);
        cpds.setPassword(password);

        SharedPoolDataSource tds = new SharedPoolDataSource();
        tds.setConnectionPoolDataSource(cpds);
        tds.setMaxActive(10);
        tds.setMaxWait(50);

        return tds;
    }
}
