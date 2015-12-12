package eu.peppol.util;

import eu.peppol.security.PkiVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

import static eu.peppol.util.GlobalConfigurationImpl.PropertyDef.*;

/**
 * Implementation of global configuration of Oxalis to be used by both stand alone and web components.
 * <p>
 * With this class, the concept of an Oxalis home directory is introduced.
 * <p/>
 * <p>See {@link OxalisHomeDirectory} for a description on how the Oxalis home directory is located.</p>
 * <p>
 * <p>
 * This class holds an inner class (enum) {@link PropertyDef}, which defines all the known runtime configurable
 * properties.
 * <p>
 * User: steinar
 * Date: 08.02.13
 * Time: 12:45
 */
public class GlobalConfigurationImpl implements GlobalConfiguration {


    /**
     * Can not make this static, but there is no need either, since this class is a singleton
     */
    public final Logger log = LoggerFactory.getLogger(GlobalConfigurationImpl.class);

    public static final String OXALIS_GLOBAL_PROPERTIES_FILE_NAME = "oxalis-global.properties";

    protected Properties properties;
    private final File oxalisGlobalPropertiesFileName;
    private volatile boolean hasBeenVerfied = false;
    private File oxalisHomeDirectory;


    public GlobalConfigurationImpl() {

        log.info("Initialising the Oxalis global configuration ....");

        // Figures out the Oxalis home directory
        oxalisHomeDirectory = computeAndSetOxalisHomeDirectory();

        // Figures out the full path and name of the Oxalis global properties file
        oxalisGlobalPropertiesFileName = computeOxalisGlobalPropertiesFileName(oxalisHomeDirectory);

        createPropertiesWithReasonableDefaults();

        loadPropertiesFromFile();

        modifyProperties();

        areAllRequiredPropertiesSet();

        logProperties();
    }

    /** Normally the Oxalis Global Properties file resides in the Oxalis home directory */
    protected File computeOxalisGlobalPropertiesFileName(File homeDirectory) {
        log.info("Oxalis home directory: " + homeDirectory);
        return new File(homeDirectory, OXALIS_GLOBAL_PROPERTIES_FILE_NAME);
    }

    protected File computeAndSetOxalisHomeDirectory() {
        return new OxalisHomeDirectory().locateDirectory();
    }

    protected void loadPropertiesFromFile() {

        if (!oxalisGlobalPropertiesFileName.isFile() || !oxalisGlobalPropertiesFileName.canRead()) {
            log.error("Unable to load the Oxalis global configuration from " + oxalisGlobalPropertiesFileName.getAbsolutePath());
            throw new IllegalStateException("Unable to locate the Global configuration file: " + oxalisGlobalPropertiesFileName.getAbsolutePath());
        }

        loadPropertiesFromFile(oxalisGlobalPropertiesFileName);
    }

    protected void modifyProperties() {

        // TransmissionBuilderOverride may be set to true if in TEST mode or the "secret" property
        // has been set.
        if (OperationalMode.TEST.equals(getModeOfOperation()) ||
                "trUe".equalsIgnoreCase(System.getenv("oxalis.transmissionbuilder.override"))) {
            log.warn("Running with transmissionBuilderOverride enabled since ENVIRONMENT variable oxalis.transmissionbuilder.override=TRUE or mode=TEST" );
            properties.setProperty(TRANSMISSION_BUILDER_OVERRIDE.getPropertyName(), Boolean.TRUE.toString());
        }
    }

    protected void createPropertiesWithReasonableDefaults() {
        properties = new Properties(PropertyDef.getDefaultPropertyValues());
        properties.setProperty(KEYSTORE_PATH.getPropertyName(), oxalisHomeDirectory + "/oxalis-keystore.jks");
    }

    protected void areAllRequiredPropertiesSet() {
        if (hasBeenVerfied)
            return;

        log.info("Verifying properties ....");
        for (PropertyDef propertyDef : PropertyDef.values()) {
            if (propertyDef.isRequired() && propertyDef.dumpValue(properties) == null) {
                throw new IllegalStateException("Property " + propertyDef.getPropertyName() + " is required, please inspect " + oxalisGlobalPropertiesFileName);
            }
        }
        hasBeenVerfied = true;
    }


    protected void loadPropertiesFromFile(File propFile) throws IllegalStateException {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(propFile), Charset.forName("UTF-8"));
            properties.load(inputStreamReader);

        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to open " + propFile + "; " + e, e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read from " + propFile + "; " + e, e);
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new IllegalStateException("Unable to close file " + oxalisGlobalPropertiesFileName);
                }
            }
        }
    }

    /** Allows access to the internal properties object for any extended classes */
    protected Properties getProperties() {
        return properties;
    }

    void logProperties() {
        for (PropertyDef propertyDef : PropertyDef.values()) {
            if (!propertyDef.isHidden()) {
                log.info(propertyDef.propertyName + " = " + propertyDef.dumpValue(properties));
            }
        }
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String getJdbcDriverClassName() {
        return JDBC_DRIVER_CLASS.getValue(properties);
    }

    @Override
    public String getJdbcConnectionURI() {
        return JDBC_URI.getValue(properties);
    }

    @Override
    public String getJdbcUsername() {
        return JDBC_USER.getValue(properties);
    }

    @Override
    public String getJdbcPassword() {
        return JDBC_PASSWORD.getValue(properties);
    }

    /**
     * @deprecated supporting JNDI data sources is going to be deprecated
     */
    @Override
    public String getDataSourceJndiName() {
        return JNDI_DATA_SOURCE.getValue(properties);
    }

    @Override
    public String getJdbcDriverClassPath() {
        return JDBC_DRIVER_CLASS_PATH.getValue(properties);
    }

    @Override
    public String getJdbcDialect() {
        return JDBC_DIALECT.getValue(properties);
    }

    /**
     * Location of the Difi private key, which belongs to oxalis-statistics-public.key
     *
     * @return path to location of private key.
     */
    @Override
    public String getStatisticsPrivateKeyPath() {
        return STATISTICS_PRIVATE_KEY_PATH.getValue(properties);
    }

    @Override
    public String getKeyStoreFileName() {
        return KEYSTORE_PATH.getValue(properties);
    }

    @Override
    public String getKeyStorePassword() {
        return KEYSTORE_PASSWORD.getValue(properties);
    }

    @Override
    public String getTrustStorePassword() {
        return TRUSTSTORE_PASSWORD.getValue(properties);
    }

    @Override
    public String getInboundMessageStore() {
        return INBOUND_MESSAGE_STORE.getValue(properties);
    }

    @Override
    public String getPersistenceClassPath() {
        return OXALIS_PERSISTENCE_CLASS_PATH.getValue(properties);
    }

    @Override
    public String getInboundLoggingConfiguration() {
        return INBOUND_LOGGING_CONFIG.getValue(properties);
    }

    @Override
    public PkiVersion getPkiVersion() {
        return PkiVersion.valueOf(PKI_VERSION.getValue(properties));
    }

    @Override
    public OperationalMode getModeOfOperation() {
        return OperationalMode.valueOf(OPERATION_MODE.getValue(properties));
    }

    @Override
    public Integer getConnectTimeout() {
        return Integer.parseInt(CONNECTION_TIMEOUT.getValue(properties));
    }

    @Override
    public Integer getReadTimeout() {
        return Integer.parseInt(READ_TIMEOUT.getValue(properties));
    }

    @Override
    public File getOxalisHomeDir() {
        return oxalisHomeDirectory;
    }

    @Override
    public String getSmlHostname() {
        return SML_HOSTNAME.getValue(properties);
    }

    @Override
    public void setSmlHostname(String hostname) {
        properties.setProperty(SML_HOSTNAME.getPropertyName(), hostname);
    }

    @Override
    public String getValidationQuery() {
        return JDBC_VALIDATION_QUERY.getValue(properties);
    }

    @Override
    public Boolean isTransmissionBuilderOverride() {
        return Boolean.valueOf(TRANSMISSION_BUILDER_OVERRIDE.getValue(properties));
    }

    /**
     * This is here to assist UNIT tests only, and should NOT be used in production.
     * Makes it possible to override in runtime as well as using environment variable
     */
    @Override
    public void setTransmissionBuilderOverride(Boolean transmissionBuilderOverride) {
        properties.setProperty(TRANSMISSION_BUILDER_OVERRIDE.getPropertyName(), transmissionBuilderOverride.toString());
    }


    /**
     * Property definitions, which are declared separately from the actual instances of
     * the properties.
     */
    public static enum PropertyDef {
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
         * PKI version to use V1, T (transition) or V2
         */
        PKI_VERSION("oxalis.pki.version", true, PkiVersion.V2.name()),

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
        TRANSMISSION_BUILDER_OVERRIDE("oxalis.transmissionbuilder.override", false, "false");

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
                throw new IllegalStateException("Property '" + propertyName + "' does not exist or is empty, check " + OXALIS_GLOBAL_PROPERTIES_FILE_NAME);
            }
            return value.trim();
        }

        String dumpValue(Properties properties) {
            return properties.getProperty(propertyName);
        }

        static Properties getDefaultPropertyValues() {

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


}
