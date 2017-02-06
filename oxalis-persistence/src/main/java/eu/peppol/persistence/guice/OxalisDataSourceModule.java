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

import com.google.inject.*;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import eu.peppol.persistence.datasource.DbcpDataSourceProvider;
import eu.peppol.persistence.datasource.JndiDataSourceProvider;
import eu.peppol.persistence.util.PersistenceConf;
import no.difi.oxalis.api.config.Settings;
import no.difi.oxalis.commons.config.builder.SettingsBuilder;

import javax.sql.DataSource;

/**
 * Guice module providing a {@link DataSource} configured and bound to an SQL DBMS
 * based upon the contents of a configuration file.
 *
 * @author steinar
 *         Date: 28.10.2016
 *         Time: 09.07
 */
public class OxalisDataSourceModule extends AbstractModule {

    @Override
    protected void configure() {

        SettingsBuilder.with(binder(), PersistenceConf.class, "Persistence")
                .add(PersistenceConf.DATASOURCE, "oxalis.database.datasource")
                .add(PersistenceConf.DRIVER_CLASS, "oxalis.jdbc.driver.class")
                .add(PersistenceConf.DRIVER_PATH, "oxalis.jdbc.class.path")
                .add(PersistenceConf.JDBC_CONNECTION_URI, "oxalis.jdbc.connection.uri")
                .add(PersistenceConf.JDBC_USERNAME, "oxalis.jdbc.user")
                .add(PersistenceConf.JDBC_PASSWORD, "oxalis.jdbc.password")
                .add(PersistenceConf.POOL_VALIDATION_QUERY, "oxalis.jdbc.validation.query")
                .add(PersistenceConf.JNDI_RESOURCE, "oxalis.datasource.jndi.name");

        bind(Key.get(DataSource.class, Names.named("dbcp")))
                .toProvider(DbcpDataSourceProvider.class)
                .in(Singleton.class);

        bind(Key.get(DataSource.class, Names.named("jndi")))
                .toProvider(JndiDataSourceProvider.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    protected DataSource dataSourceProvider(Injector injector, Settings<PersistenceConf> settings) {
        return injector.getInstance(
                Key.get(DataSource.class, settings.getNamed(PersistenceConf.DATASOURCE)));
    }
}
