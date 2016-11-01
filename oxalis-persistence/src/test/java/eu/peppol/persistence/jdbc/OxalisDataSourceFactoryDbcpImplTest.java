/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.persistence.jdbc;

import eu.peppol.jdbc.OxalisDataSourceFactory;
import eu.peppol.persistence.guice.TestModuleFactory;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.GlobalConfigurationImpl;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
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
@Guice(moduleFactory = TestModuleFactory.class)
public class OxalisDataSourceFactoryDbcpImplTest {

    GlobalConfiguration globalConfiguration;

    @Inject
    OxalisDataSourceFactory oxalisDataSourceFactory;

    @Inject
    OxalisDataSourceFactory oxalisDataSourceFactory2;

    @BeforeClass
    public void setUp() {

        globalConfiguration = GlobalConfigurationImpl.getInstance();
        assertNotNull(globalConfiguration);
    }

    @Test
    public void oxalisDataSourceFactoryIsSingleton() throws Exception {

        // Attempts to load the first instance of OxalisDataSourceFactory
        assertNotNull(oxalisDataSourceFactory);

        // Second invocation should return same instance
        assertEquals(oxalisDataSourceFactory, oxalisDataSourceFactory2, "Seems the Singletong pattern in OxalisDataSourceFactoryProvider is not working");

        // The datasource should also be the same instance
        DataSource dataSource1 = oxalisDataSourceFactory.getDataSource();
        assertNotNull(dataSource1);
        DataSource dataSource2 = oxalisDataSourceFactory.getDataSource();

        assertEquals(dataSource1, dataSource2, OxalisDataSourceFactory.class.getSimpleName() + " is not returning a singleton instance of DataSource");

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

        ObjectName poolName = new ObjectName("no.difi.oxalis", "connectionPool", "TestPool");
        PoolableConnectionFactory factory = new PoolableConnectionFactory(driverConnectionFactory, poolName);

        GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<>(factory);
        factory.setPool(pool);

        pool.setMaxTotal(10);
        pool.setMaxWaitMillis(100);


        assertEquals(pool.getFactory(), factory);

        PoolableConnectionFactory pcf = (PoolableConnectionFactory) ((GenericObjectPool<?>) pool).getFactory();
        ObjectPool<PoolableConnection> pool1 = pcf.getPool();

        PoolingDataSource<PoolableConnection> poolingDataSource = new PoolingDataSource<>(pool);

        Connection connection = poolingDataSource.getConnection();
        assertNotNull(connection);

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select current_date()");

        assertTrue(resultSet.next());
    }


    @Test
    public void testFailWithStaleConnection() throws Exception {
        ConnectionFactory driverConnectionFactory = createConnectionFactory(false);


        PoolingDataSource poolingDataSource = createPoolingDataSource(driverConnectionFactory);
        try {
            runTwoSqlStatementsWithTwoConnections(poolingDataSource);
        } catch (Exception e) {
            assertTrue(e.getClass().getName().contains("CommunicationsException"));
        }
    }

    @Test
    public void testHandleStaleConnections() throws Exception {
        ConnectionFactory driverConnectionFactory = createConnectionFactory(true);

        PoolingDataSource poolingDataSource = createPoolingDataSource(driverConnectionFactory);

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
        } catch (SQLException e) {
            // As expected when using DBCP 1.4
        }
    }

    private void runTwoSqlStatementsWithTwoConnections(PoolingDataSource poolingDataSource) throws SQLException, InterruptedException {

        Connection connection = poolingDataSource.getConnection();
        if (connection.getMetaData().getDatabaseProductName().toLowerCase().contains("mysql")) {
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


    private PoolingDataSource createPoolingDataSource(ConnectionFactory driverConnectionFactory) {

        PoolableConnectionFactory poolableConnectionFactory = null;
        try {

            poolableConnectionFactory = new PoolableConnectionFactory(driverConnectionFactory, new ObjectName("no.difi.oxalis", "connectionPool", "TestPool"));

            GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<PoolableConnection>(poolableConnectionFactory);
            poolableConnectionFactory.setPool(pool);
            poolableConnectionFactory.setValidationQuery("select 1");
            return new PoolingDataSource(pool);

        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException("Unable to create poolable conneciton factory: " + e.getMessage(), e);
        }

    }

}
