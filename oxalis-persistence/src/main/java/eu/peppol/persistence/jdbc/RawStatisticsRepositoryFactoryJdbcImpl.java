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

import eu.peppol.persistence.guice.jdbc.JdbcTxManager;
import eu.peppol.persistence.guice.jdbc.Repository;
import no.difi.oxalis.api.statistics.RawStatisticsRepository;
import no.difi.oxalis.api.statistics.RawStatisticsRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * StatisticsRepositoryFactory implementation which uses an SQL based data model
 * to which access is gained via JDBC.
 *
 * <p>This implementation is based upon using the dependency injection annotation {@link Named}
 * in order make the dependency framework do the heavy lifting for us.
 * </p>
 * <p>
 *     The solution is not elegant, but it works with Google Guice at least
 *
 * </p>
 *
 * @author steinar
 * @author thore
 */
@Repository
public class RawStatisticsRepositoryFactoryJdbcImpl implements RawStatisticsRepositoryFactory {

    public static final Logger log = LoggerFactory.getLogger(RawStatisticsRepositoryFactoryJdbcImpl.class);

    private final JdbcTxManager jdbcTxManager;

    @Inject
    @Named("MySQL")
    RawStatisticsRepository mySql;

    @Inject
    @Named("H2")
    RawStatisticsRepository h2;

    @Inject
    @Named("MsSql")
    RawStatisticsRepository msSql;

    @Inject
    @Named("Oracle")
    RawStatisticsRepository oracle;

    @Inject
    @Named("HSqlDB")
    RawStatisticsRepository hsqlDb;



    @Inject
    public RawStatisticsRepositoryFactoryJdbcImpl(final JdbcTxManager jdbcTxManager) {
        this.jdbcTxManager = jdbcTxManager;
    }


    @Override
    public RawStatisticsRepository getInstanceForRawStatistics() {

        String databaseProductName;
        try {
            DatabaseMetaData metaData = jdbcTxManager.getConnection().getMetaData();
            databaseProductName = metaData.getDatabaseProductName().toLowerCase();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        /**
         * Note! This does not work with Guice's method interceptor. Must be refactored.
         *
         * The Factory pattern can not be used because Guice needs to instantiate the objects in order to inject
         * the Transactional AOP stuff.
         */

/*
        // Pre Google Guice's AOP stuff:

        if ("H2".equalsIgnoreCase(sqlDialect) ) return new RawStatisticsRepositoryMsSqlImpl(jdbcTxManager);
        if ("MySql".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryMySqlImpl(jdbcTxManager);
        if ("MsSql".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryMsSqlImpl(jdbcTxManager);
        if ("Oracle".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryOracleImpl(jdbcTxManager);
        if ("HSqlDB".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryHSqlImpl(jdbcTxManager);
*/

        if (databaseProductName.contains("h2")) return h2;
        if (databaseProductName.contains("mysql")) return mySql;
        if (databaseProductName.contains("microsoft")) return msSql;
        if (databaseProductName.contains("oracle")) return oracle;
        if (databaseProductName.contains("hsql")) return hsqlDb;

        throw new IllegalArgumentException("Unsupportet jdbc dialect " + databaseProductName);
    }

}
