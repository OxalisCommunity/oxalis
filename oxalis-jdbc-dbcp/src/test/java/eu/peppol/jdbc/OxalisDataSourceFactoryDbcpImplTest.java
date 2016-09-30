/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.jdbc;

import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.GlobalConfigurationImpl;
import org.apache.commons.dbcp.*;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

    GlobalConfiguration globalConfiguration;

    @BeforeClass
    public void setUp() {

        globalConfiguration =  GlobalConfigurationImpl.getInstance();
        assertNotNull(globalConfiguration);
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
        URL url = new URL(jdbcDriverClassPath);
        try {
            File file = new File(url.toURI());
            if (!file.exists()) {
                throw new IllegalStateException("JDBC driver class path not found: " + file);
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to convert URL " + url.toExternalForm() + " into URI: " + e.getMessage(), e);
        }
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url}, Thread.currentThread().getContextClassLoader());


        String jdbcDriverClassName = globalConfiguration.getJdbcDriverClassName();
        String connectURI = globalConfiguration.getJdbcConnectionURI(); // + "?initialTimeout=2";
        String userName = globalConfiguration.getJdbcUsername();
        String password = globalConfiguration.getJdbcPassword();

        Class<?> aClass = null;
        try {
            aClass = Class.forName(jdbcDriverClassName, true, urlClassLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to locate class " + jdbcDriverClassName + " in class path '" + jdbcDriverClassPath + "'");
        }
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
