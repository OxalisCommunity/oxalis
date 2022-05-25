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

package network.oxalis.persistence.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.guice.OxalisModule;
import network.oxalis.commons.settings.SettingsBuilder;
import network.oxalis.persistence.datasource.DbcpDataSourceProvider;
import network.oxalis.persistence.datasource.JndiDataSourceProvider;
import network.oxalis.persistence.util.PersistenceConf;

import javax.sql.DataSource;

/**
 * Guice module providing a {@link DataSource} configured and bound to an SQL DBMS
 * based upon the contents of a configuration file.
 *
 * @author steinar
 * @author erlend
 */
public class DataSourceModule extends OxalisModule {

    @Override
    protected void configure() {
        SettingsBuilder.with(binder(), PersistenceConf.class);

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
