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

package eu.peppol.persistence.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import eu.peppol.lang.OxalisLoadingException;
import eu.peppol.persistence.annotation.Repository;
import eu.peppol.persistence.api.JdbcTxManager;
import no.difi.oxalis.api.statistics.RawStatisticsRepository;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author erlend
 */
@Repository
class RawStatisticsRepositoryProvider implements Provider<RawStatisticsRepository> {

    @Inject
    private JdbcTxManager jdbcTxManager;

    @Inject
    private Injector injector;

    @Override
    public RawStatisticsRepository get() {
        String databaseProductName;
        try {
            DatabaseMetaData metaData = jdbcTxManager.getConnection().getMetaData();
            databaseProductName = metaData.getDatabaseProductName().toLowerCase();
        } catch (SQLException e) {
            throw new OxalisLoadingException("Unable to fetch database product name.", e);
        }

        if (databaseProductName.contains("mysql"))
            return injector.getInstance(
                    Key.get(RawStatisticsRepository.class, Names.named(RawStatisticsRepositoryModule.MYSQL)));
        if (databaseProductName.contains("microsoft"))
            return injector.getInstance(
                    Key.get(RawStatisticsRepository.class, Names.named(RawStatisticsRepositoryModule.MSSQL)));
        if (databaseProductName.contains("oracle"))
            return injector.getInstance(
                    Key.get(RawStatisticsRepository.class, Names.named(RawStatisticsRepositoryModule.ORACLE)));
        if (databaseProductName.contains("hsql"))
            return injector.getInstance(
                    Key.get(RawStatisticsRepository.class, Names.named(RawStatisticsRepositoryModule.HSQLDB)));
        if (databaseProductName.contains("h2"))
            return injector.getInstance(
                    Key.get(RawStatisticsRepository.class, Names.named(RawStatisticsRepositoryModule.H2)));

        throw new OxalisLoadingException(String.format("Unsupported jdbc dialect '%s'.", databaseProductName));
    }
}
