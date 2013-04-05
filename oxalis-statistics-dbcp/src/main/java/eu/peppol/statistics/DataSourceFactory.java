package eu.peppol.statistics;

import eu.peppol.util.GlobalConfiguration;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;

import javax.sql.DataSource;

/**
 * @author steinar
 *         Date: 05.04.13
 *         Time: 10:23
 */
class DataSourceFactory {

    private final GlobalConfiguration globalConfiguration;

    DataSourceFactory() {
        this.globalConfiguration = GlobalConfiguration.getInstance();

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

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(connectURI);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
        basicDataSource.setDriverClassLoader(this.getClass().getClassLoader());


        basicDataSource.setMaxActive(10);
        basicDataSource.setMaxWait(50);

        return basicDataSource;
    }
}
