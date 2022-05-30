/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.persistence.datasource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.filesystem.ClassLoaderUtils;
import network.oxalis.persistence.testng.PersistenceModuleFactory;
import network.oxalis.persistence.util.PersistenceConf;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;

import static org.testng.Assert.*;

/**
 * TODO Enable testing.
 *
 * @author steinar
 *         Date: 18.04.13
 *         Time: 14:08
 */
@Guice(moduleFactory = PersistenceModuleFactory.class)
public class DbcpDataSourceProviderTest {

    @Inject
    private Provider<DataSource> dataSourceProvider;

    @Inject
    private Provider<DataSource> dataSourceProvider2;

    @Inject
    private Settings<PersistenceConf> settings;

    @Inject
    @Named("home")
    private Path homeFolder;

    @BeforeClass
    public void setUp() {
        assertNotNull(settings);
    }

    @Test
    public void oxalisDataSourceFactoryIsSingleton() throws Exception {

        // Attempts to load the first instance of DataSourceProvider
        assertNotNull(dataSourceProvider);

        // Second invocation should return same instance
        assertEquals(dataSourceProvider, dataSourceProvider2,
                "Seems the Singleton pattern in DataSourceProviderFactory is not working");

        // The datasource should also be the same instance
        DataSource dataSource1 = dataSourceProvider.get();
        assertNotNull(dataSource1);
        DataSource dataSource2 = dataSourceProvider.get();

        assertEquals(dataSource1, dataSource2,
                dataSourceProvider.getClass().getSimpleName() + " is not returning a singleton instance of DataSource");

    }

    /**
     * Verifies that we can create a pooled jdbc data source using the
     * JDBC .jar-file supplied in the global configuration file.
     */
    @Test
    public void testLoadJdbcDriverUsingCustomClassLoader() throws Exception {
        ConnectionFactory driverConnectionFactory = createConnectionFactory(false);

        ObjectName poolName = new ObjectName("network.oxalis", "connectionPool", "TestPool");
        PoolableConnectionFactory factory = new PoolableConnectionFactory(driverConnectionFactory, poolName);

        GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<>(factory);
        factory.setPool(pool);

        pool.setMaxTotal(10);
        pool.setMaxWaitMillis(100);


        assertEquals(pool.getFactory(), factory);

        PoolableConnectionFactory pcf = (PoolableConnectionFactory) ((GenericObjectPool<?>) pool).getFactory();
        //ObjectPool<PoolableConnection> pool1 =
        pcf.getPool();

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

    @Test(enabled = false)
    public void testHandleStaleConnections() throws Exception {
        ConnectionFactory driverConnectionFactory = createConnectionFactory(true);

        PoolingDataSource poolingDataSource = createPoolingDataSource(driverConnectionFactory);

        runTwoSqlStatementsWithTwoConnections(poolingDataSource);
    }

    @Test
    public void testBasicDataSource() throws Exception {

        Path jdbcDriverClassPath = settings.getPath(PersistenceConf.DRIVER_PATH, homeFolder);
        ClassLoader classLoader = ClassLoaderUtils.initiate(jdbcDriverClassPath);

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(settings.getString(PersistenceConf.DRIVER_CLASS));
        basicDataSource.setUrl(settings.getString(PersistenceConf.JDBC_CONNECTION_URI));
        basicDataSource.setUsername(settings.getString(PersistenceConf.JDBC_USERNAME));
        basicDataSource.setPassword(settings.getString(PersistenceConf.JDBC_PASSWORD));

        // Does not work in 1.4, fixed in 1.4.1
        basicDataSource.setDriverClassLoader(classLoader);

        try {
            Connection connection = basicDataSource.getConnection();
            assertNotNull(connection);
        } catch (SQLException e) {
            // As expected when using DBCP 1.4
        }
    }

    private void runTwoSqlStatementsWithTwoConnections(PoolingDataSource poolingDataSource)
            throws SQLException, InterruptedException {

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
            statement.executeQuery("select current_time()");
        }
    }


    private ConnectionFactory createConnectionFactory(boolean profileSql) throws MalformedURLException,
            ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        Path jdbcDriverClassPath = settings.getPath(PersistenceConf.DRIVER_PATH, homeFolder);

        ClassLoader classLoader = ClassLoaderUtils.initiate(jdbcDriverClassPath);

        String jdbcDriverClassName = settings.getString(PersistenceConf.DRIVER_CLASS);
        URI connectURI = URI.create(settings.getString(PersistenceConf.JDBC_CONNECTION_URI)); // + "?initialTimeout=2";
        String userName = settings.getString(PersistenceConf.JDBC_USERNAME);
        String password = settings.getString(PersistenceConf.JDBC_PASSWORD);

        Class<?> aClass;
        try {
            aClass = Class.forName(jdbcDriverClassName, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(String.format(
                    "Unable to locate class '%s' in class path '%s'", jdbcDriverClassName, jdbcDriverClassPath));
        }
        Driver driver = (Driver) aClass.newInstance();
        assertTrue(driver.acceptsURL(connectURI.toString()));

        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);
        if (profileSql) {
            properties.put("profileSQL", "true");       // MySQL debug option
        }
        return new DriverConnectionFactory(driver, connectURI.toString(), properties);
    }


    @SuppressWarnings("unchecked")
    private PoolingDataSource createPoolingDataSource(ConnectionFactory driverConnectionFactory) {

        PoolableConnectionFactory poolableConnectionFactory;
        try {

            poolableConnectionFactory = new PoolableConnectionFactory(driverConnectionFactory,
                    new ObjectName("network.oxalis", "connectionPool", "TestPool"));

            GenericObjectPool<PoolableConnection> pool = new GenericObjectPool<>(poolableConnectionFactory);
            poolableConnectionFactory.setPool(pool);
            poolableConnectionFactory.setValidationQuery("select 1");
            return new PoolingDataSource(pool);
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException("Unable to create poolable conneciton factory: " + e.getMessage(), e);
        }
    }
}
