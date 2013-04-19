package eu.peppol.jdbc;

import eu.peppol.util.GlobalConfiguration;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Properties;

/**
 * Provides an instance of {@link DataSource} using the condfiguration parameters found
 * in {@link GlobalConfiguration#OXALIS_GLOBAL_PROPERTIES}, which is located in
 * OXALIS_HOME.
 *
 *
 * @author steinar
 *         Date: 18.04.13
 *         Time: 13:28
 */
public class OxalisDataSourceFactoryDbcpImpl {


    /**
     * Creates a DataSource with connection pooling as provided by Apache DBCP
     *
     * @return a DataSource
     */
    public DataSource getDataSource() {

        GlobalConfiguration globalConfiguration = GlobalConfiguration.getInstance();

        String jdbcDriverClassPath = globalConfiguration.getJdbcDriverClassPath();

        URLClassLoader urlClassLoader = getOxalisClassLoaderForJdbc(jdbcDriverClassPath);


        String className = globalConfiguration.getJdbcDriverClassName();
        String connectURI = globalConfiguration.getConnectionURI();
        String userName = globalConfiguration.getUserName();
        String password = globalConfiguration.getPassword();

        // Loads the JDBC Driver in a separate class loader
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
