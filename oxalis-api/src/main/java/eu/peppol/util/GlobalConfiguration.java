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

import java.io.File;

/**
 * @author steinar
 *         Date: 09.12.2015
 *         Time: 10.31
 */
public interface GlobalConfiguration {

    String getJdbcDriverClassName();

    String getJdbcConnectionURI();

    String getJdbcUsername();

    String getJdbcPassword();

    String getDataSourceJndiName();

    String getJdbcDriverClassPath();

    String getJdbcDialect();

    /** Name of file holding the keystore in which our certificate resides (the access point certificate) */
    String getKeyStoreFileName();

    /** Password for our access point certificate key store */
    String getKeyStorePassword();

    /** TODO: remove this and replace with constant in PeppolTrustStoreLoader */
    String getTrustStorePassword();

    /** Where to persist the inbound messages */
    String getInboundMessageStore();

    String getPersistenceClassPath();

    String getInboundLoggingConfiguration();

    OperationalMode getModeOfOperation();

    Integer getConnectTimeout();

    Integer getReadTimeout();

    /** Name of Oxalis home directory */
    File getOxalisHomeDir();

    String getSmlHostname();


    /** HTTP Proxy configuration */
    String getHttpProxyHost();

    String getHttpProxyPort();

    String getProxyUser();

    String getProxyPassword();

    /** ------------------ end of proxy config */


    String getValidationQuery();

    /** Indicates whether your may override the values in the SBDH when creating a transmission builder */
    Boolean isTransmissionBuilderOverride();

    void setTransmissionBuilderOverride(Boolean transmissionBuilderOverride);
}
