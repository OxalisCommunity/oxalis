package eu.peppol.jdbc;

import eu.peppol.util.GlobalConfiguration;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 18.04.13
 *         Time: 14:08
 */
@Test(groups = {"integration"})
public class OxalisDataSourceFactoryDbcpImplTest {

    private GlobalConfiguration globalConfiguration;

    @BeforeTest
    public void setUp() {
        globalConfiguration = GlobalConfiguration.getInstance();
    }

    /**
     * Verifies that we can create a pooled jdbc data source using the JDBC .jar-file supplied in the global configuration
     * file.
     *
     * @throws Exception
     */
    @Test
    public void testLoadJdbcDriverUsingCustomClassLoader() throws Exception {
        String jdbcDriverClassPath = globalConfiguration.getJdbcDriverClassPath();
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL(jdbcDriverClassPath)}, Thread.currentThread().getContextClassLoader());


        String className = globalConfiguration.getJdbcDriverClassName();
        String connectURI = globalConfiguration.getJdbcConnectionURI();
        String userName = globalConfiguration.getJdbcUsername();
        String password = globalConfiguration.getJdbcPassword();

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
