/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
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

import eu.peppol.security.PkiVersion;

import java.io.File;

/**
 * @author steinar
 *         Date: 09.12.2015
 *         Time: 10.31
 */
public interface GlobalConfiguration {
    String getProperty(String key);

    String getJdbcDriverClassName();

    String getJdbcConnectionURI();

    String getJdbcUsername();

    String getJdbcPassword();

    String getDataSourceJndiName();

    String getJdbcDriverClassPath();

    String getJdbcDialect();

    String getStatisticsPrivateKeyPath();

    String getKeyStoreFileName();

    String getKeyStorePassword();

    String getTrustStorePassword();

    String getInboundMessageStore();

    String getPersistenceClassPath();

    String getInboundLoggingConfiguration();

    PkiVersion getPkiVersion();

    OperationalMode getModeOfOperation();

    Integer getConnectTimeout();

    Integer getReadTimeout();

    File getOxalisHomeDir();

    String getSmlHostname();

    String getValidationQuery();

    Boolean isTransmissionBuilderOverride();

    void setTransmissionBuilderOverride(Boolean transmissionBuilderOverride);
}
