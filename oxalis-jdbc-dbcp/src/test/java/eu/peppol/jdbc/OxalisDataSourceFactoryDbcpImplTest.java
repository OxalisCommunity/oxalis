package eu.peppol.jdbc;

import eu.peppol.util.GlobalConfiguration;
import org.apache.commons.dbcp.*;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.Properties;

import static org.testng.Assert.*;

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
        ConnectionFactory driverConnectionFactory = createConnectionFactory(false);

        GenericObjectPool genericObjectPool = new GenericObjectPool(null);

        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(driverConnectionFactory, genericObjectPool, null, "select 1", false, true);
        PoolingDataSource poolingDataSource = new PoolingDataSource(genericObjectPool);

        Connection connection = poolingDataSource.getConnection();
        assertNotNull(connection);

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select current_date()");

        assertTrue(resultSet.next());
    }



    @Test
    public void testFailWithStaleConnection() throws Exception {
        ConnectionFactory driverConnectionFactory = createConnectionFactory(false);

        GenericObjectPool genericObjectPool = new GenericObjectPool(null);
        genericObjectPool.setMaxActive(1);



        PoolingDataSource poolingDataSource = createPoolingDataSource(driverConnectionFactory, genericObjectPool);
        try {
            runTwoSqlStatementsWithTwoConnections(poolingDataSource);
        } catch (Exception e) {
            assertTrue(e.getClass().getName().contains("CommunicationsException"));
        }
    }

    @Test
    public void testHandleStaleConnections() throws Exception {
        ConnectionFactory driverConnectionFactory = createConnectionFactory(true);

        GenericObjectPool genericObjectPool = new GenericObjectPool(null);
        genericObjectPool.setMaxActive(1);

        genericObjectPool.setTestOnBorrow(true);
/*
        genericObjectPool.setTestWhileIdle(true);   // Test idle instances visited by the pool maintenance thread and destroy any that fail validation
        genericObjectPool.setTimeBetweenEvictionRunsMillis(10000);      // Test every 10 seconds
        genericObjectPool.setMaxWait(10000);
*/

        PoolingDataSource poolingDataSource = createPoolingDataSource(driverConnectionFactory, genericObjectPool);
        runTwoSqlStatementsWithTwoConnections(poolingDataSource);
    }

    @Test
    public void testBasicDataSource() throws Exception {

        String jdbcDriverClassPath = globalConfiguration.getJdbcDriverClassPath();
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL(jdbcDriverClassPath)}, Thread.currentThread().getContextClassLoader());

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(globalConfiguration.getJdbcDriverClassName());
        basicDataSource.setUrl(globalConfiguration.getJdbcConnectionURI());
        basicDataSource.setUsername(globalConfiguration.getJdbcUsername());
        basicDataSource.setPassword(globalConfiguration.getJdbcPassword());

        // Does not work in 1.4, fixed in 1.4.1
        basicDataSource.setDriverClassLoader(urlClassLoader);

        try {
            Connection connection = basicDataSource.getConnection();
            assertNotNull(connection);
            fail("Wuhu! They have finally fixed the bug in DBCP; ignoring the classloader. Consider changing the code!");
        } catch (SQLException e) {
            // As expected when using DBCP 1.4
        }
    }

    private void runTwoSqlStatementsWithTwoConnections(PoolingDataSource poolingDataSource) throws SQLException, InterruptedException {
        Connection connection = poolingDataSource.getConnection();
        assertNotNull(connection);

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select current_date()");

        statement = connection.createStatement();
        statement.execute("set session wait_timeout=1");
        assertTrue(resultSet.next());

        connection.close(); // return to pool

        // Wait for 2 seconds
        System.err.print("Sleeping for 2 seconds....");
        Thread.sleep(2 * 1000L);
        System.err.println("Running again now");
        connection = poolingDataSource.getConnection();
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select current_time()");
    }

    private ConnectionFactory createConnectionFactory(boolean profileSql) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        String jdbcDriverClassPath = globalConfiguration.getJdbcDriverClassPath();
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL(jdbcDriverClassPath)}, Thread.currentThread().getContextClassLoader());


        String jdbcDriverClassName = globalConfiguration.getJdbcDriverClassName();
        String connectURI = globalConfiguration.getJdbcConnectionURI(); // + "?initialTimeout=2";
        String userName = globalConfiguration.getJdbcUsername();
        String password = globalConfiguration.getJdbcPassword();

        Class<?> aClass = Class.forName(jdbcDriverClassName, true, urlClassLoader);
        Driver driver = (Driver) aClass.newInstance();
        assertTrue(driver.acceptsURL(connectURI));

        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);
        if (profileSql) {
            properties.put("profileSQL", "true");       // MySQL debug option
        }
        return new DriverConnectionFactory(driver, connectURI, properties);
    }


    private PoolingDataSource createPoolingDataSource(ConnectionFactory driverConnectionFactory, GenericObjectPool genericObjectPool) {

        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(driverConnectionFactory, genericObjectPool, null, null, false, true);
        poolableConnectionFactory.setValidationQuery("select 1");

        return new PoolingDataSource(genericObjectPool);
    }

}
