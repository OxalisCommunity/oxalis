package eu.peppol.statistics;

import eu.peppol.util.GlobalConfiguration;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

/**
 * Provides StatisticsRepository objects, ready to use with a DataSource injected etc.
 * <p/>
 * This implementation will use DBCP to create a connection pool.
 * <p/>
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


    /**
     * Creates a DataSource using the properties found in the global Oxalis configuration.
     *
     * @return
     */
    DataSource setupDataSourceFromGlobalConfiguration() {
        String jdbcDriverClassPath = globalConfiguration.getJdbcDriverClassPath();

        URLClassLoader urlClassLoader = getOxalisClassLoaderForJdbc(jdbcDriverClassPath);


        String className = globalConfiguration.getJdbcDriverClassName();
        String connectURI = globalConfiguration.getConnectionURI();
        String userName = globalConfiguration.getUserName();
        String password = globalConfiguration.getPassword();

        Driver driver = getJdbcDriver(jdbcDriverClassPath, urlClassLoader, className);

        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);

        // DBCP factory which will produce JDBC Driver instances
        ConnectionFactory driverConnectionFactory = new DriverConnectionFactory(driver, connectURI, properties);

        // DBCP object pool holding our driver connections
        GenericObjectPool genericObjectPool = new GenericObjectPool(null);
        genericObjectPool.setMaxActive(50);
        genericObjectPool.setMaxWait(2000);

        // DBCP Factory holding the pooled connection, which are created by the driver connection factory and held in the supplied pool
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(driverConnectionFactory, genericObjectPool, null, null, false, true);
        PoolingDataSource poolingDataSource = new PoolingDataSource(genericObjectPool);
        return poolingDataSource;

    }

    private Driver getJdbcDriver(String jdbcDriverClassPath, URLClassLoader urlClassLoader, String className) {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(className, true, urlClassLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to locate class " + className + " in " + jdbcDriverClassPath);
        }
        Driver driver = null;
        try {
            driver = (Driver) aClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Unable to instantiate driver from class " + className,e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access driver class " + className + "; "+e, e);
        }
        return driver;
    }

    private URLClassLoader getOxalisClassLoaderForJdbc(String jdbcDriverClassPath) {
        URLClassLoader urlClassLoader = null;

        try {
            urlClassLoader = new URLClassLoader(new URL[]{new URL(jdbcDriverClassPath)}, Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid jdbc driver class path: '"+ jdbcDriverClassPath +"', check property oxalis.jdbc.class.path");
        }
        return urlClassLoader;
    }

}
