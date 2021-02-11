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
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.filesystem.ClassLoaderUtils;
import network.oxalis.persistence.util.PersistenceConf;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.impl.GenericObjectPool;

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
 * Relies upon Guice to make sure provided DataSource is threated as a singleton.
 *
 * @author steinar
 * Date: 18.04.13
 * Time: 13:28
 * @author erlend
 */
@Slf4j
public class DbcpDataSourceProvider implements Provider<DataSource> {

    private final Settings<PersistenceConf> settings;

    private final Path homeFolder;

    @Inject
    public DbcpDataSourceProvider(Settings<PersistenceConf> settings, @Named("home") Path homeFolder) {
        this.settings = settings;
        this.homeFolder = homeFolder;

        log.info("DataSource: {} ", settings.getString(PersistenceConf.JDBC_CONNECTION_URI));
    }

    /**
     * Creates a DataSource with connection pooling as provided by Apache DBCP
     *
     * @return a DataSource
     */
    public DataSource get() {

        log.debug("Configuring DataSource wrapped in a Database Connection Pool, using custom loader");

        Path jdbcDriverClassPath = settings.getPath(PersistenceConf.DRIVER_PATH, homeFolder);

        log.debug("Loading JDBC Driver with custom class path: " + jdbcDriverClassPath);
        // Creates a new class loader, which will be used for loading our JDBC driver
        ClassLoader classLoader = ClassLoaderUtils.initiate(jdbcDriverClassPath);

        String className = settings.getString(PersistenceConf.DRIVER_CLASS);
        String connectURI = settings.getString(PersistenceConf.JDBC_CONNECTION_URI);

        // Loads the JDBC Driver in a separate class loader
        Driver driver = getJdbcDriver(classLoader, className);

        Properties properties = new Properties();
        properties.put("user", settings.getString(PersistenceConf.JDBC_USERNAME));
        properties.put("password", settings.getString(PersistenceConf.JDBC_PASSWORD));

        // DBCP factory which will produce JDBC Driver instances
        ConnectionFactory driverConnectionFactory = new DriverConnectionFactory(driver, connectURI, properties);

        // DBCP Factory holding the pooled connection, which are created by the driver connection
        // factory and held in the supplied pool
        ObjectName dataSourceJmxName;
        try {
            dataSourceJmxName = new ObjectName("network.oxalis", "connectionPool", "OxalisDB");
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(driverConnectionFactory, dataSourceJmxName);

        String validationQuery = settings.getString(PersistenceConf.DBCP_VALIDATION_QUERY);
        if (validationQuery != null) {
            poolableConnectionFactory.setValidationQuery(validationQuery);
        }
        // DBCP object pool holding our driver connections
        GenericObjectPool<PoolableConnection> genericObjectPool = new GenericObjectPool<>(poolableConnectionFactory);
        poolableConnectionFactory.setPool(genericObjectPool);
        genericObjectPool.setMaxTotal(settings.getInt(PersistenceConf.DBCP_MAX_TOTAL));
        genericObjectPool.setMaxIdle(settings.getInt(PersistenceConf.DBCP_MAX_IDLE));
        genericObjectPool.setMaxWaitMillis(10000);

        // Test the connection returned from the pool
        genericObjectPool.setTestOnBorrow(true);

        // Test idle instances visited by the pool maintenance thread and destroy any that fail validation
        genericObjectPool.setTestWhileIdle(true);

        // Test every hour
        genericObjectPool.setTimeBetweenEvictionRunsMillis(60 * 60 * 1000);

        // Creates the actual DataSource instance
        return new PoolingDataSource(genericObjectPool);
    }

    private static Driver getJdbcDriver(ClassLoader classLoader, String className) {
        try {
            return (Driver) Class.forName(className, true, classLoader).newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to locate class " + className + ".", e);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Unable to instantiate driver from class " + className, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access driver class " + className + "; " + e, e);
        }
    }
}
