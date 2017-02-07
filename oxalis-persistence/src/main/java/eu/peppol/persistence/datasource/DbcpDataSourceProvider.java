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

package eu.peppol.persistence.datasource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import eu.peppol.persistence.util.PersistenceConf;
import no.difi.oxalis.api.config.Settings;
import no.difi.oxalis.commons.filesystem.ClassLoaderUtils;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Driver;
import java.util.Properties;

/**
 * Given a set configuration parameters represented by {@link Settings}, this class will
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
public class DbcpDataSourceProvider implements Provider<DataSource> {

    public static final Logger log = LoggerFactory.getLogger(DbcpDataSourceProvider.class);

    private final Settings<PersistenceConf> settings;

    private volatile DataSource dataSource;

    @Inject
    public DbcpDataSourceProvider(Settings<PersistenceConf> settings) {
        this.settings = settings;
        log.info("DataSource: {} ", settings.getString(PersistenceConf.JDBC_CONNECTION_URI));

    }

    @Override
    public DataSource get() {
        if (dataSource == null) {
            synchronized (this) {
                if (dataSource == null) {
                    dataSource = configureAndCreateDataSource();
                }
            }
        }
        return dataSource;
    }

    /**
     * Creates a DataSource with connection pooling as provided by Apache DBCP
     *
     * @return a DataSource
     */
    private DataSource configureAndCreateDataSource() {

        log.debug("Configuring DataSource wrapped in a Database Connection Pool, using custom loader");

        Path jdbcDriverClassPath = settings.getPath(PersistenceConf.DRIVER_PATH);

        log.debug("Loading JDBC Driver with custom class path: " + jdbcDriverClassPath);
        // Creates a new class loader, which will be used for loading our JDBC driver
        ClassLoader classLoader = getOxalisClassLoaderForJdbc(jdbcDriverClassPath);


        String className = settings.getString(PersistenceConf.DRIVER_CLASS);
        String connectURI = settings.getString(PersistenceConf.JDBC_CONNECTION_URI);
        String userName = settings.getString(PersistenceConf.JDBC_USERNAME);
        String password = settings.getString(PersistenceConf.JDBC_PASSWORD);

        log.debug("className=" + className);
        log.debug("connectURI=" + connectURI);
        log.debug("userName=" + userName);
        log.debug("password=" + password);

        // Loads the JDBC Driver in a separate class loader
        Driver driver = getJdbcDriver(jdbcDriverClassPath, classLoader, className);

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

        String validationQuery = settings.getString(PersistenceConf.POOL_VALIDATION_QUERY);
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

    private static Driver getJdbcDriver(Path jdbcDriverClassPath, ClassLoader urlClassLoader, String className) {
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

    private static ClassLoader getOxalisClassLoaderForJdbc(Path jdbcDriverClassPath) {
        return ClassLoaderUtils.initiate(jdbcDriverClassPath);
    }
}
