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
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Properties;

/**
 * Provides an instance of {@link DataSource} using the configuration parameters found
 * in {@link GlobalConfigurationImpl#OXALIS_GLOBAL_PROPERTIES_FILE_NAME}, which is located in
 * OXALIS_HOME.
 *
 * Thread safe and singleton. I.e. will always return the same DataSource.
 *
 * @author steinar
 *         Date: 18.04.13
 *         Time: 13:28
 */
public class OxalisDataSourceFactoryDbcpImpl implements OxalisDataSourceFactory {

    public static final Logger log = LoggerFactory.getLogger(OxalisDataSourceFactoryDbcpImpl.class);


    private static class DataSourceHolder {
        private static final DataSource INSTANCE = OxalisDataSourceFactoryDbcpImpl.configureAndCreateDataSource();
    }

    @Override
    public DataSource getDataSource() {
        return DataSourceHolder.INSTANCE;
    }

    /**
     * Creates a DataSource with connection pooling as provided by Apache DBCP
     *
     * @return a DataSource
     */
    public static DataSource configureAndCreateDataSource() {

        log.debug("Configuring DataSource wrapped in a Database Connection Pool, using custom loader");

        GlobalConfiguration globalConfiguration = GlobalConfigurationImpl.getInstance();

        String jdbcDriverClassPath = globalConfiguration.getJdbcDriverClassPath();

        log.debug("Loading JDBC Driver with custom class path: " + jdbcDriverClassPath);
        // Creates a new class loader, which will be used for loading our JDBC driver
        URLClassLoader urlClassLoader = getOxalisClassLoaderForJdbc(jdbcDriverClassPath);


        String className = globalConfiguration.getJdbcDriverClassName();
        String connectURI = globalConfiguration.getJdbcConnectionURI();
        String userName = globalConfiguration.getJdbcUsername();
        String password = globalConfiguration.getJdbcPassword();

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

        // DBCP object pool holding our driver connections
        GenericObjectPool genericObjectPool = new GenericObjectPool(null);
        genericObjectPool.setMaxActive(100);
        genericObjectPool.setMaxIdle(30);
        genericObjectPool.setMaxWait(10000);

        genericObjectPool.setTestOnBorrow(true);    // Test the connection returned from the pool

        genericObjectPool.setTestWhileIdle(true);   // Test idle instances visited by the pool maintenance thread and destroy any that fail validation
        genericObjectPool.setTimeBetweenEvictionRunsMillis(60 * 60 * 1000);      // Test every hour

        // DBCP Factory holding the pooled connection, which are created by the driver connection factory and held in the supplied pool
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(driverConnectionFactory, genericObjectPool, null, null, false, true);

        String validationQuery = globalConfiguration.getValidationQuery();
        poolableConnectionFactory.setValidationQuery(validationQuery);

        // Creates the actual DataSource instance
        PoolingDataSource poolingDataSource = new PoolingDataSource(genericObjectPool);

        return poolingDataSource;

    }

    private static Driver getJdbcDriver(String jdbcDriverClassPath, URLClassLoader urlClassLoader, String className) {
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

    private static URLClassLoader getOxalisClassLoaderForJdbc(String jdbcDriverClassPath) {
        URLClassLoader urlClassLoader = null;

        try {
            urlClassLoader = new URLClassLoader(new URL[]{new URL(jdbcDriverClassPath)}, Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid jdbc driver class path: '"+ jdbcDriverClassPath +"', check property oxalis.jdbc.class.path. Cause: " +e.getMessage(), e);
        }
        return urlClassLoader;
    }


}
