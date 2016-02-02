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

package eu.peppol.persistence.sql;

import eu.peppol.jdbc.OxalisDataSourceFactory;
import eu.peppol.jdbc.OxalisDataSourceFactoryProvider;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.GlobalConfigurationImpl;

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
    private GlobalConfiguration globalConfiguration;

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
            globalConfiguration = GlobalConfigurationImpl.getInstance();
        }

        assert globalConfiguration != null : "global configuration property is null!";

        String sqlDialect = globalConfiguration.getJdbcDialect().toLowerCase();
        if ("MySql".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryMySqlImpl(dataSource);
        if ("MsSql".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryMsSqlImpl(dataSource);
	    if ("Oracle".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryOracleImpl(dataSource);
	    if ("HSqlDB".equalsIgnoreCase(sqlDialect)) return new RawStatisticsRepositoryHSqlImpl(dataSource);
		throw new IllegalArgumentException("Unsupportet jdbc dialect " + sqlDialect);
    }

}
