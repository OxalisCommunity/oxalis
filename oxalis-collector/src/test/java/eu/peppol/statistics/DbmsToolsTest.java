package eu.peppol.statistics;

import eu.peppol.util.GlobalConfiguration;
import org.apache.commons.dbcp.*;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 03.04.13
 *         Time: 21:40
 */
public class DbmsToolsTest {

    private GlobalConfiguration globalConfiguration;

    @BeforeTest
    public void setUp() {
        globalConfiguration = GlobalConfiguration.getInstance();

    }
    @Test
    public void testCreateDatabaseSchema() throws Exception {

        DbmsTools dbmsTools = new DbmsTools();

        dbmsTools.createDatabaseSchema();
    }

    @Test
    public void testLoadJdbcDriverUsingCustomClassLoader() throws Exception {
        String jdbcDriverClassPath = globalConfiguration.getJdbcDriverClassPath();
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL(jdbcDriverClassPath)}, Thread.currentThread().getContextClassLoader());


        String className = globalConfiguration.getJdbcDriverClassName();
        String connectURI = globalConfiguration.getConnectionURI();
        String userName = globalConfiguration.getUserName();
        String password = globalConfiguration.getPassword();

        Class<?> aClass = Class.forName(className, true, urlClassLoader);
        Driver driver = (Driver) aClass.newInstance();
        assertTrue(driver.acceptsURL(connectURI));

        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);

        ConnectionFactory driverConnectionFactory = new DriverConnectionFactory(driver, connectURI, properties);

        GenericObjectPool genericObjectPool = new GenericObjectPool(null);

        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(driverConnectionFactory, genericObjectPool, null, null, false, true);
        PoolingDataSource poolingDataSource = new PoolingDataSource(genericObjectPool);
        Connection connection = poolingDataSource.getConnection();
        assertNotNull(connection);


    }


}
