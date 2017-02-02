/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.peppol.persistence.jdbc;

import eu.peppol.jdbc.OxalisDataSourceFactory;
import no.difi.oxalis.api.persistence.RepositoryConfiguration;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Properties;

/**
 * Given a set configuration parameters represented by {@link RepositoryConfiguration}, this class will
 * provide a DataSource wrapped in a DataSource pool.
 * <p>
 * <p>
 * Thread safe and singleton. I.e. will always return the same DataSource.
 * </p>
 *
 * @author steinar
 *         Date: 18.04.13
 *         Time: 13:28
 */
public class OxalisDataSourceFactoryDbcpImpl implements OxalisDataSourceFactory {

    public static final Logger log = LoggerFactory.getLogger(OxalisDataSourceFactoryDbcpImpl.class);

    private final RepositoryConfiguration configuration;

    private volatile DataSource dataSource;

    @Inject
    public OxalisDataSourceFactoryDbcpImpl(RepositoryConfiguration configuration) {
        this.configuration = configuration;
        log.info("DataSource being connected to " + configuration.getJdbcConnectionUri().toString());

    }

    @Override
    public DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (this) {
                if (dataSource == null) {
                    dataSource = configureAndCreateDataSource(configuration);
                }
            }
        }
        return dataSource;
    }

    @Override
    public boolean isProvidedWithOxalisDistribution() {
        // This is the one and only supplied as default.
        return true;
    }

    /**
     * Creates a DataSource with connection pooling as provided by Apache DBCP
     *
     * @return a DataSource
     */
    DataSource configureAndCreateDataSource(RepositoryConfiguration configuration) {

        log.debug("Configuring DataSource wrapped in a Database Connection Pool, using custom loader");


        String jdbcDriverClassPath = configuration.getJdbcDriverClassPath();

        log.debug("Loading JDBC Driver with custom class path: " + jdbcDriverClassPath);
        // Creates a new class loader, which will be used for loading our JDBC driver
        URLClassLoader urlClassLoader = getOxalisClassLoaderForJdbc(jdbcDriverClassPath);


        String className = configuration.getJdbcDriverClassName();
        String connectURI = configuration.getJdbcConnectionUri().toString();
        String userName = configuration.getJdbcUsername();
        String password = configuration.getJdbcPassword();

        log.debug("className=" + className);
        log.debug("connectURI=" + connectURI);
        log.debug("userName=" + userName);
        log.debug("password=" + password);

        // Loads the JDBC Driver in a separate class loader
        Driver driver = getJdbcDriver(jdbcDriverClassPath, urlClassLoader, className);

        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);

        // DBCP factory which will produce JDBC Driver instances
        ConnectionFactory driverConnectionFactory = new DriverConnectionFactory(driver, connectURI, properties);


        // DBCP Factory holding the pooled connection, which are created by the driver connection factory and held in the supplied pool
        ObjectName dataSourceJmxName;
        try {
            dataSourceJmxName = new ObjectName("no.difi.oxalis", "connectionPool", "OxalisDB");
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(driverConnectionFactory, dataSourceJmxName);

        String validationQuery = configuration.getValidationQuery();
        if (validationQuery != null) {
            poolableConnectionFactory.setValidationQuery(validationQuery);
        }
        // DBCP object pool holding our driver connections
        GenericObjectPool<PoolableConnection> genericObjectPool = new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(genericObjectPool);
        genericObjectPool.setMaxTotal(100);
        genericObjectPool.setMaxIdle(30);
        genericObjectPool.setMaxWaitMillis(10000);

        genericObjectPool.setTestOnBorrow(true);    // Test the connection returned from the pool

        genericObjectPool.setTestWhileIdle(true);   // Test idle instances visited by the pool maintenance thread and destroy any that fail validation
        genericObjectPool.setTimeBetweenEvictionRunsMillis(60 * 60 * 1000);      // Test every hour

        // Creates the actual DataSource instance
        return new PoolingDataSource(genericObjectPool);
    }

    private static Driver getJdbcDriver(String jdbcDriverClassPath, URLClassLoader urlClassLoader, String className) {
        Class<?> aClass;
        try {
            aClass = Class.forName(className, true, urlClassLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to locate class " + className + " in " + jdbcDriverClassPath);
        }
        Driver driver;
        try {
            driver = (Driver) aClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Unable to instantiate driver from class " + className, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access driver class " + className + "; " + e, e);
        }
        return driver;
    }

    private static URLClassLoader getOxalisClassLoaderForJdbc(String jdbcDriverClassPath) {
        URLClassLoader urlClassLoader;

        try {
            urlClassLoader = new URLClassLoader(new URL[]{new URL(jdbcDriverClassPath)}, Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid jdbc driver class path: '" + jdbcDriverClassPath + "', check property oxalis.jdbc.class.path. Cause: " + e.getMessage(), e);
        }
        return urlClassLoader;
    }
}
