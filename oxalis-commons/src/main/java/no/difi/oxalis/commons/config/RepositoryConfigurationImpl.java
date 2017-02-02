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

package no.difi.oxalis.commons.config;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import no.difi.oxalis.api.config.GlobalConfiguration;
import no.difi.oxalis.api.persistence.RepositoryConfiguration;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author erlend
 */
class RepositoryConfigurationImpl implements RepositoryConfiguration {

    private final Config config;

    @Inject
    public RepositoryConfigurationImpl(GlobalConfiguration configuration, Config config) {
        this.config = config;
    }

    @Override
    public Path getBasePath() {
        return Paths.get(config.getString("oxalis.inbound.message.store"));
    }

    @Override
    public String getJdbcDriverClassName() {
        return config.getString("oxalis.jdbc.driver.class");
    }

    @Override
    public URI getJdbcConnectionUri() {
        return URI.create(config.getString("oxalis.jdbc.connection.uri"));
    }

    @Override
    public String getJdbcDriverClassPath() {
        return config.getString("oxalis.jdbc.class.path");
    }

    @Override
    public String getJdbcUsername() {
        return config.getString("oxalis.jdbc.user");
    }

    @Override
    public String getJdbcPassword() {
        return config.getString("oxalis.jdbc.password");
    }

    @Override
    public String getValidationQuery() {
        return config.getString("oxalis.jdbc.validation.query");
    }

    @Override
    public String getDataSourceJndiName() {
        if (config.hasPath("oxalis.datasource.jndi.name"))
            return config.getString("oxalis.datasource.jndi.name");

        return null;
    }
}
