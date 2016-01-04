package eu.peppol.persistence.sql;

import eu.peppol.util.GlobalConfiguration;
import eu.peppol.jdbc.OxalisDataSourceFactory;
import eu.peppol.jdbc.OxalisDataSourceFactoryProvider;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;

import javax.sql.DataSource;

/**
 * StatisticsRepositoryFactory implementation which uses an SQL based data model
 * to which access is gained via JDBC.
 *
 * <p>The JDBC DataSource is obtained using the META-INF/services method</p>
 *
 * @author steinar
 * @author thore
 */
public class RawStatisticsRepositoryFactoryJdbcImpl implements RawStatisticsRepositoryFactory {

    private DataSource dataSource;

    public RawStatisticsRepositoryFactoryJdbcImpl() {
        // we intentionally don't initialize anything here (including dataSource),
        // since this service could be the first loaded by the ServiceLoader and
        // we will skip it use the next one instead.
    }

    @Override
    public RawStatisticsRepository getInstanceForRawStatistics() {
        if (dataSource == null) {
            OxalisDataSourceFactory oxalisDataSourceFactory = OxalisDataSourceFactoryProvider.getInstance();
            dataSource = oxalisDataSourceFactory.getDataSource();
        }
		GlobalConfiguration globalConfiguration = GlobalConfiguration.getInstance();
		String sqlDialect = globalConfiguration.getJdbcDialect().toLowerCase();
		if ("MySql".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryMySqlImpl(dataSource);
        if ("MsSql".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryMsSqlImpl(dataSource);
	    if ("Oracle".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryOracleImpl(dataSource);
	    if ("HSqlDB".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryHSqlImpl(dataSource);
		throw new IllegalArgumentException("Unsupportet jdbc dialect " + sqlDialect);
    }

}
