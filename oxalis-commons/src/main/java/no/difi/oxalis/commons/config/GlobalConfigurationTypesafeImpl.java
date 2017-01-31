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
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;

import java.io.File;
import java.nio.file.Path;

/**
 * @author erlend
 */
public class GlobalConfigurationTypesafeImpl implements GlobalConfiguration {

    private Config config;

    private Path homePath;

    private Boolean override = false;

    @Inject
    public GlobalConfigurationTypesafeImpl(Config config, @Named("home") Path homePath) {
        this.config = config;
        this.homePath = homePath;
    }

    @Override
    public String getJdbcDriverClassName() {
        return config.getString("oxalis.jdbc.driver.class");
    }

    @Override
    public String getJdbcConnectionURI() {
        return config.getString("oxalis.jdbc.connection.uri");
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
    public String getDataSourceJndiName() {
        if (config.hasPath("oxalis.datasource.jndi.name"))
            return config.getString("oxalis.datasource.jndi.name");

        return null;
    }

    @Override
    public String getJdbcDriverClassPath() {
        return config.getString("oxalis.jdbc.class.path");
    }

    @Override
    public String getKeyStoreFileName() {
        return config.getString("keystore.path");
    }

    @Override
    public String getKeyStorePassword() {
        return config.getString("keystore.password");
    }

    @Override
    public String getInboundMessageStore() {
        return config.getString("oxalis.inbound.message.store");
    }

    @Override
    public String getPersistenceClassPath() {
        if (config.hasPath("oxalis.persistence.class.path"))
            return config.getString("oxalis.persistence.class.path");

        return null;
    }

    @Override
    public String getInboundLoggingConfiguration() {
        return config.getString("oxalis.inbound.log.config");
    }

    @Override
    public OperationalMode getModeOfOperation() {
        return OperationalMode.valueOf(config.getString("oxalis.operation.mode"));
    }

    @Override
    public File getOxalisHomeDir() {
        return homePath.toFile();
    }

    @Override
    public String getHttpProxyHost() {
        if (config.hasPath("oxalis.httpProxyHost"))
            return config.getString("oxalis.httpProxyHost");

        return null;
    }

    @Override
    public String getHttpProxyPort() {
        if (config.hasPath("oxalis.httpProxyPort"))
            return config.getString("oxalis.httpProxyPort");

        return null;
    }

    @Override
    public String getProxyUser() {
        if (config.hasPath("oxalis.proxyUser"))
            return config.getString("oxalis.proxyUser");

        return null;
    }

    @Override
    public String getProxyPassword() {
        if (config.hasPath("oxalis.proxyPassword"))
            return config.getString("oxalis.proxyPassword");

        return null;
    }

    @Override
    public String getValidationQuery() {
        return config.getString("oxalis.jdbc.validation.query");
    }

    @Override
    public Boolean isTransmissionBuilderOverride() {
        return override;
    }

    @Override
    public void setTransmissionBuilderOverride(Boolean transmissionBuilderOverride) {
        this.override = transmissionBuilderOverride;
    }
}
