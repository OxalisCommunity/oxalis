package eu.peppol.persistence.sql;

import eu.peppol.jdbc.OxalisDataSourceFactory;
import eu.peppol.jdbc.OxalisDataSourceFactoryProvider;
import eu.peppol.statistics.StatisticsRepository;
import eu.peppol.statistics.StatisticsRepositoryFactory;

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
public class StatisticsRepositoryFactoryJdbcImpl implements StatisticsRepositoryFactory {


    private final DataSource dataSource;

    public StatisticsRepositoryFactoryJdbcImpl() {
        OxalisDataSourceFactory oxalisDataSourceFactory = OxalisDataSourceFactoryProvider.getInstance();
        dataSource = oxalisDataSourceFactory.getDataSource();
    }

    @Override
    public StatisticsRepository getInstance() {
        return new StatisticsRepositoryJdbcImpl(dataSource);
    }
}
