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
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;

import java.io.File;

/**
 * @author erlend
 */
public class GlobalConfigurationTypesafeImpl implements GlobalConfiguration {

    private Config config;

    private Boolean override;

    @Inject
    public GlobalConfigurationTypesafeImpl(Config config) {
        this.config = config;
    }

    @Override
    public String getJdbcDriverClassName() {
        return null;
    }

    @Override
    public String getJdbcConnectionURI() {
        return null;
    }

    @Override
    public String getJdbcUsername() {
        return null;
    }

    @Override
    public String getJdbcPassword() {
        return null;
    }

    @Override
    public String getDataSourceJndiName() {
        return null;
    }

    @Override
    public String getJdbcDriverClassPath() {
        return null;
    }

    @Override
    public String getKeyStoreFileName() {
        return null;
    }

    @Override
    public String getKeyStorePassword() {
        return null;
    }

    @Override
    public String getInboundMessageStore() {
        return null;
    }

    @Override
    public String getPersistenceClassPath() {
        return null;
    }

    @Override
    public String getInboundLoggingConfiguration() {
        return "logback-test.xml";
    }

    @Override
    public OperationalMode getModeOfOperation() {
        return null;
    }

    @Override
    public Integer getConnectTimeout() {
        return null;
    }

    @Override
    public Integer getReadTimeout() {
        return null;
    }

    @Override
    public File getOxalisHomeDir() {
        return null;
    }

    @Override
    public String getHttpProxyHost() {
        return null;
    }

    @Override
    public String getHttpProxyPort() {
        return null;
    }

    @Override
    public String getProxyUser() {
        return null;
    }

    @Override
    public String getProxyPassword() {
        return null;
    }

    @Override
    public String getValidationQuery() {
        return null;
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
