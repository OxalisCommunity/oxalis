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
import no.difi.oxalis.api.config.GlobalConfiguration;
import no.difi.oxalis.api.config.OperationalMode;

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
    public String getKeyStoreFileName() {
        return config.getString("keystore.path");
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
    public Boolean isTransmissionBuilderOverride() {
        return override;
    }

    @Override
    public void setTransmissionBuilderOverride(Boolean transmissionBuilderOverride) {
        this.override = transmissionBuilderOverride;
    }
}
