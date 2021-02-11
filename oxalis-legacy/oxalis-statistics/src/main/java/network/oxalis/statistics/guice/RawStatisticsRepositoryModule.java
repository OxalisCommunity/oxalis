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

package network.oxalis.statistics.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import network.oxalis.commons.guice.OxalisModule;
import network.oxalis.persistence.api.Platform;
import network.oxalis.persistence.guice.AopJdbcTxManagerModule;
import network.oxalis.persistence.platform.*;
import network.oxalis.statistics.jdbc.RawStatisticsRepositoryHSqlImpl;
import network.oxalis.statistics.jdbc.RawStatisticsRepositoryMySqlImpl;
import network.oxalis.statistics.api.RawStatisticsRepository;
import network.oxalis.statistics.jdbc.RawStatisticsRepositoryMsSqlImpl;
import network.oxalis.statistics.jdbc.RawStatisticsRepositoryOracleImpl;

/**
 * Wires up the persistence component.
 * <p>
 * NOTE! When creating an injector, remember to supply an instance of {@link javax.sql.DataSource}
 *
 * @author steinar
 * Date: 25.10.2016
 * Time: 21.43
 */
public class RawStatisticsRepositoryModule extends OxalisModule {

    @Override
    protected void configure() {
        // Includes the Aop based Tx manager, which needs a DataSource
        binder().install(new AopJdbcTxManagerModule());

        bind(Key.get(RawStatisticsRepository.class, Names.named(H2Platform.IDENTIFIER)))
                .to(RawStatisticsRepositoryMsSqlImpl.class);

        bind(Key.get(RawStatisticsRepository.class, Names.named(MySQLPlatform.IDENTIFIER)))
                .to(RawStatisticsRepositoryMySqlImpl.class);

        bind(Key.get(RawStatisticsRepository.class, Names.named(MsSQLPlatform.IDENTIFIER)))
                .to(RawStatisticsRepositoryMsSqlImpl.class);

        bind(Key.get(RawStatisticsRepository.class, Names.named(OraclePlatform.IDENTIFIER)))
                .to(RawStatisticsRepositoryOracleImpl.class);

        bind(Key.get(RawStatisticsRepository.class, Names.named(HSQLDBPlatform.IDENTIFIER)))
                .to(RawStatisticsRepositoryHSqlImpl.class);
    }

    @Provides
    @Singleton
    public RawStatisticsRepository get(Injector injector, Platform platform) {
        return injector.getInstance(Key.get(RawStatisticsRepository.class, platform.getNamed()));
    }
}
