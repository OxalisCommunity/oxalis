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


    DataSource setupDataSourceFromGlobalConfiguration() {
        String jdbcDriverClassPath = globalConfiguration.getJdbcDriverClassPath();
        URLClassLoader urlClassLoader = null;
        try {
            urlClassLoader = new URLClassLoader(new URL[]{new URL(jdbcDriverClassPath)}, Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid jdbc driver class path: '"+ jdbcDriverClassPath +"', check property oxalis.jdbc.class.path");
        }


        String className = globalConfiguration.getJdbcDriverClassName();
        String connectURI = globalConfiguration.getConnectionURI();
        String userName = globalConfiguration.getUserName();
        String password = globalConfiguration.getPassword();

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

        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);

        ConnectionFactory driverConnectionFactory = new DriverConnectionFactory(driver, connectURI, properties);

        GenericObjectPool genericObjectPool = new GenericObjectPool(null);
        genericObjectPool.setMaxActive(50);
        genericObjectPool.setMaxWait(2000);

        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(driverConnectionFactory, genericObjectPool, null, null, false, true);
        PoolingDataSource poolingDataSource = new PoolingDataSource(genericObjectPool);
        return poolingDataSource;

    }

}
