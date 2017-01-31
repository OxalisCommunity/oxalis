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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.jdbc.OxalisDataSourceFactory;
import eu.peppol.persistence.jdbc.OxalisDataSourceFactoryDbcpImpl;

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
        bind(OxalisDataSourceFactory.class).to(OxalisDataSourceFactoryDbcpImpl.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    protected DataSource dataSourceProvider(OxalisDataSourceFactory oxalisDataSourceFactory) {
        return oxalisDataSourceFactory.getDataSource();
    }
}
