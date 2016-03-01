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

/**
 * @author steinar
 *         Date: 14.12.2015
 *         Time: 16.52
 */

import java.util.Properties;

/**
 * Property definitions, which are declared separately from the actual instances of
 * the properties.
 */
public enum PropertyDef {
    /**
     * Location of Java keystore holding our private key and signed certificate
     */
    KEYSTORE_PATH("oxalis.keystore", true),

    /**
     * The password of the above keystore
     */
    KEYSTORE_PASSWORD("oxalis.keystore.password", true, "peppol", false),

    TRUSTSTORE_PASSWORD("oxalis.truststore.password", false, "peppol", false),

    /**
     * Where to store inbound messages
     */
    INBOUND_MESSAGE_STORE("oxalis.inbound.message.store", true, System.getProperty("java.io.tmpdir") + "inbound"),

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
     * The SQL dialect used at the backend of JDBC connection.
     */
    JDBC_DIALECT("oxalis.jdbc.dialect", false, "mysql", false),

    /**
     * Name of JNDI Data Source
     */
    @Deprecated()
    JNDI_DATA_SOURCE("oxalis.datasource.jndi.name", false),

    /**
     * Location of private RSA key used within the statistics module
     */
    STATISTICS_PRIVATE_KEY_PATH("oxalis.statistics.private.key", false),

    /**
     * Location of Logback configuration file for inbound server
     */
    INBOUND_LOGGING_CONFIG("oxalis.inbound.log.config", true, "logback-oxalis-server.xml"),

    /**
     * Location of Logback configuration file for standalone applications
     */
    APP_LOGGING_CONFIG("oxalis.app.log.config", false, "logback-oxalis.xml"),

    /**
     * Mode of operation, i.e. TEST or PRODUCTION.
     * For PKI version 1, TEST is the only mode available.
     */
    OPERATION_MODE("oxalis.operation.mode", true, OperationalMode.TEST.name()),

    /**
     * The timeout value in milliseconds, to be used when opening the http connection to the receiving
     * access point. The default is 5 seconds.
     * A value of 0 means infinite timeout.
     *
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/java/net/URLConnection.html#setConnectTimeout(int)">URLConnection.html#setConnectTimeout(int)</a>
     */
    CONNECTION_TIMEOUT("oxalis.connection.timeout", false, "5000"),

    /**
     * Read timeout value in milliseconds. If the number of milliseconds elapses before data is available for read,
     * a timeout exception will be thrown. A value of 0 is interpreted as an infinite timeout.
     *
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/java/net/URLConnection.html#setReadTimeout(int)">URLConnection.html#setReadTimeout(int)</a>
     */
    READ_TIMEOUT("oxalis.read.timeout", false, "5000"),

    /**
     * Will override SML hostname if defined in properties file. Makes it possible to route trafic to other SMLs
     * than the official SMLs.
     * <p>
     * Example: oxalis.sml.hostname=sml.peppolcentral.org
     */
    SML_HOSTNAME("oxalis.sml.hostname", false, "", false),

    /**
     * Whether overriding the properties of the transmission builder is allowed.
     */
    TRANSMISSION_BUILDER_OVERRIDE("oxalis.transmissionbuilder.override", false, "false"),

    /**
     * The http proxy host
     */
    HTTP_PROXY_HOST("oxalis.httpProxyHost", false),

    /**
     * The http proxy port
     */
    HTTP_PROXY_PORT("oxalis.httpProxyPort", false),

    /**
     * The proxy user
     */
    PROXY_USER("oxalis.proxyUser", false),

    /**
     * The proxy password
     */
    PROXY_PASSWORD("oxalis.proxyPassword", false);

    /**
     * External name of property as it appears in your .properties file, i.e. with the dot notation,
     * like for instance "x.y.z = value"
     */
    private String propertyName;
    private boolean required;
    private final String defaultValue;
    private boolean hidden = false;

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
            throw new IllegalStateException("Property '" + propertyName + "' does not exist or is empty, check your config file (oxalis-global.properties perhaps?)");
        }
        return value.trim();
    }

    String dumpValue(Properties properties) {
        return properties.getProperty(propertyName);
    }

    public static Properties getDefaultPropertyValues() {

        Properties defaultProperties = new Properties();
        for (PropertyDef propertyDef : values()) {
            if (defaultProperties.getProperty(propertyDef.propertyName) == null) {
                defaultProperties.setProperty(propertyDef.propertyName, propertyDef.defaultValue);
            }
        }
        return defaultProperties;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
