package eu.peppol.persistence.sql;

import eu.peppol.util.GlobalConfiguration;
import eu.peppol.jdbc.OxalisDataSourceFactory;
import eu.peppol.jdbc.OxalisDataSourceFactoryProvider;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;

import javax.sql.DataSource;

/**
 * StatisticsRepositoryFactory implementation which uses an SQL based data model to which access is gained
 * via JDBC.
 *
 * <p>The JDBC DataSource is obtained using the META-INF/services method</p>
 *
 * @author steinar
 *         Date: 18.04.13
 *         Time: 15:47
 */
public class RawStatisticsRepositoryFactoryJdbcImpl implements RawStatisticsRepositoryFactory {


    private final DataSource dataSource;

    public RawStatisticsRepositoryFactoryJdbcImpl() {
        OxalisDataSourceFactory oxalisDataSourceFactory = OxalisDataSourceFactoryProvider.getInstance();
        dataSource = oxalisDataSourceFactory.getDataSource();
    }

    @Override
    public RawStatisticsRepository getInstanceForRawStatistics() {
		GlobalConfiguration globalConfiguration = GlobalConfiguration.getInstance();
		String sqlDialect = globalConfiguration.getJdbcDialect().toLowerCase();

		if (sqlDialect.equals("mysql")) {
	        return new RawStatisticsRepositoryMySqlImpl(dataSource);
		} else if (sqlDialect.equals("mssql")) {
	        return new RawStatisticsRepositoryMsSqlImpl(dataSource);
		}

		throw new IllegalArgumentException("Unsupportet jdbc dialect " + sqlDialect);
    }

}
