/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.util;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.peppol.persistence.RepositoryConfiguration;

import javax.inject.Singleton;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author steinar
 *         Date: 28.10.2016
 *         Time: 09.58
 */
public class OxalisProductionConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides @Singleton
    GlobalConfiguration providesGlobalConfiguration() {
            return GlobalConfigurationImpl.getInstance();
    }

    @Provides @Singleton
    RepositoryConfiguration repositoryConfiguration(GlobalConfiguration configuration) {
        return new RepositoryConfiguration() {
            @Override
            public Path getBasePath() {
                return Paths.get(configuration.getInboundMessageStore());
            }

            @Override
            public URI getJdbcConnectionUri() {
                return URI.create(configuration.getJdbcConnectionURI());
            }

            @Override
            public String getJdbcDriverClassPath() {
                return configuration.getJdbcDriverClassPath();
            }

            @Override
            public String getJdbcDriverClassName() {
                return configuration.getJdbcDriverClassName();
            }

            @Override
            public String getJdbcUsername() {
                return configuration.getJdbcUsername();
            }

            @Override
            public String getJdbcPassword() {
                return configuration.getJdbcPassword();
            }

            @Override
            public String getValidationQuery() {
                return null;
            }
        };
    }
}
