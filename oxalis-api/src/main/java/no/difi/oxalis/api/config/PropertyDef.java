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

package no.difi.oxalis.api.config;

import java.util.Properties;

/**
 * Property definitions, which are declared separately from the actual instances of
 * the properties.
 *
 * @author steinar
 *         Date: 14.12.2015
 *         Time: 16.52
 */
public enum PropertyDef {

    /**
     * Class path entry where the persistence module is located.
     */
    OXALIS_PERSISTENCE_CLASS_PATH("oxalis.persistence.class.path", false),

    /**
     * Name of JDBC Driver class
     */
    JDBC_DRIVER_CLASS("oxalis.jdbc.driver.class", false),

    /**
     * The JDBC connection URL
     */
    JDBC_URI("oxalis.jdbc.connection.uri", false),

    /**
     * JDBC User name
     */
    JDBC_USER("oxalis.jdbc.user", false),

    /**
     * Jdbc password
     */
    JDBC_PASSWORD("oxalis.jdbc.password", true, "", false),

    /**
     * Location of the JDBC driver named in JDBC_DRIVER_CLASS
     */
    JDBC_DRIVER_CLASS_PATH("oxalis.jdbc.class.path", false),

    /**
     * The SQL validation query used to determine whether the JDBC connection is stale or not.
     * The actual value depends upon your JDBC driver.
     */
    JDBC_VALIDATION_QUERY("oxalis.jdbc.validation.query", false, "select 1", false),

    /**
     * Name of JNDI Data Source
     */
    JNDI_DATA_SOURCE("oxalis.datasource.jndi.name", false),

    /**
     * Location of Logback configuration file for inbound server
     */
    INBOUND_LOGGING_CONFIG("oxalis.inbound.log.config", true, "logback-oxalis-server.xml"),

    /**
     * Mode of operation, i.e. TEST or PRODUCTION.
     * For PKI version 1, TEST is the only mode available.
     */
    OPERATION_MODE("oxalis.operation.mode", true, OperationalMode.TEST.name()),

    /**
     * Whether overriding the properties of the transmission builder is allowed.
     */
    TRANSMISSION_BUILDER_OVERRIDE("oxalis.transmissionbuilder.override", false, "false");

    /**
     * External name of property as it appears in your .properties file, i.e. with the dot notation,
     * like for instance "x.y.z = value"
     */
    private String propertyName;

    private boolean required;

    private final String defaultValue;

    private boolean hidden;

    /**
     * Enum constructor
     *
     * @param propertyName name of property as it appears in your .properties file
     */
    PropertyDef(String propertyName, boolean required) {
        this(propertyName, required, null);
    }

    PropertyDef(String propertyName, boolean required, String defaultValue, boolean hidden) {
        this(propertyName, required, defaultValue);
        this.hidden = hidden;
    }

    PropertyDef(String propertyName, boolean required, String defaultValue) {
        if (propertyName == null || propertyName.trim().length() == 0) {
            throw new IllegalArgumentException("Property name is required");
        }
        this.propertyName = propertyName;
        this.required = required;
        if (defaultValue != null)
            this.defaultValue = defaultValue;
        else
            this.defaultValue = "";
    }

    /**
     * Locates the value of this named property in the supplied collection of properties.
     *
     * @param properties collection of properties to search
     * @return value of property
     */
    public String getValue(Properties properties) {
        if (required) {
            return required(properties.getProperty(propertyName));
        } else {
            String propertyValue = properties.getProperty(propertyName);
            if (propertyValue != null) {
                return propertyValue.trim();
            }
        }
        return null;
    }

    private String required(String value) {
        if (value == null || value.trim().length() == 0) {
            throw new IllegalStateException("Property '" + propertyName +
                    "' does not exist or is empty, check your config file (oxalis-global.properties perhaps?)");
        }
        return value.trim();
    }

    public boolean isRequired() {
        return required;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
