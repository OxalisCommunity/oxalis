package eu.peppol.util;

import eu.peppol.security.PkiVersion;
import eu.peppol.start.identifier.AccessPointIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

import static eu.peppol.util.GlobalConfiguration.PropertyDef.*;

/**
 * Singleton implementation of global configuration of Oxalis to be used by both stand alone and web components.
 * With this class, the concept of an Oxalis home directory is introduced.
 * <p/>
 * <p>See {@link OxalisHomeDirectory} for a description on how the Oxalis home directory is located.</p>
 * <p/>
 * User: steinar
 * Date: 08.02.13
 * Time: 12:45
 */
public enum GlobalConfiguration {

    INSTANCE;

    /** Can not make this static, but there is no need either, since this class is a singleton */
    public final Logger log = LoggerFactory.getLogger(GlobalConfiguration.class);

    public static final String OXALIS_GLOBAL_PROPERTIES = "oxalis-global.properties";

    Properties properties;
    private final File oxalisGlobalPropertiesFileName;
    private volatile boolean hasBeenVerfied = false;
    private File oxalisHomeDirectory;

    public static GlobalConfiguration getInstance() {
        // Lazy verification, i.e. verification is performed upon first call to this method
        // to prevent ExceptionInInitializerError being thrown.
        INSTANCE.verifyProperties();
        return INSTANCE;
    }

    static GlobalConfiguration getInstanceNoVerification() {
        return INSTANCE;
    }

    GlobalConfiguration() {

        System.out.println("Initialising the Oxalis global configuration ....");
        // Figures out the Oxalis home directory
        oxalisGlobalPropertiesFileName = computeOxalisHomeDir();

        loadProperties();
    }

    private File computeOxalisHomeDir() {
        oxalisHomeDirectory = new OxalisHomeDirectory().locateDirectory();
        System.out.println("Oxalis home directory: " + oxalisHomeDirectory);
        return new File(oxalisHomeDirectory, OXALIS_GLOBAL_PROPERTIES);
    }

    void loadProperties() {

        createPropertiesWithReasonableDefaults();

        if (!oxalisGlobalPropertiesFileName.isFile() || !oxalisGlobalPropertiesFileName.canRead()) {
            System.err.println("Unable to load the Oxalis global configuration from " + oxalisGlobalPropertiesFileName.getAbsolutePath());
            throw new IllegalStateException("Unable to locate the Global configuration file: " + oxalisGlobalPropertiesFileName.getAbsolutePath());
        }

        loadPropertiesFromFile(oxalisGlobalPropertiesFileName);

        logProperties();
    }

    private void createPropertiesWithReasonableDefaults() {
        properties = new Properties(PropertyDef.getDefaultPropertyValues());
        properties.setProperty(KEYSTORE_PATH.getPropertyName(), oxalisHomeDirectory + "/oxalis-keystore.jks");
    }

    synchronized void verifyProperties() {
        if (hasBeenVerfied)
            return;

        System.out.println("Verifying properties ....");
        for (PropertyDef propertyDef : PropertyDef.values()) {
            if (propertyDef.isRequired() && propertyDef.dumpValue(properties) == null) {
                throw new IllegalStateException("Property " + propertyDef.getPropertyName() + " is required, please inspect " + oxalisGlobalPropertiesFileName);
            }
        }
        hasBeenVerfied = true;
    }


    private Properties loadPropertiesFromFile(File propFile) throws IllegalStateException {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(propFile), Charset.forName("UTF-8"));
            properties.load(inputStreamReader);

            return properties;
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

    void logProperties() {
        for (PropertyDef propertyDef : PropertyDef.values()) {
            log.info(propertyDef.propertyName + " = " + propertyDef.dumpValue(properties));
        }
    }

    public String getJdbcDriverClassName() {
        return JDBC_DRIVER_CLASS.getValue(properties);
    }

    public String getJdbcConnectionURI() {
        return JDBC_URI.getValue(properties);
    }

    public String getJdbcUsername() {
        return JDBC_USER.getValue(properties);
    }

    public String getJdbcPassword() {
        return JDBC_PASSWORD.getValue(properties);
    }

    public AccessPointIdentifier getAccessPointIdentifier() {
        String accessPointIdentifierValue = AP_ID.getValue(properties);
        return new AccessPointIdentifier(accessPointIdentifierValue);
    }

    /**
     * @deprecated supporting JNDI data sources is going to be deprecated
     */
    public String getDataSourceJndiName() {
        return JNDI_DATA_SOURCE.getValue(properties);
    }

    public String getJdbcDriverClassPath() {
        return JDBC_DRIVER_CLASS_PATH.getValue(properties);
    }

    public String getStatisticsPrivateKeyPath() {
        return STATISTICS_PRIVATE_KEY_PATH.getValue(properties);
    }

    public String getKeyStoreFileName() {
        return KEYSTORE_PATH.getValue(properties);
    }

    public String getKeyStorePassword() {
        return KEYSTORE_PASSWORD.getValue(properties);
    }

    public String getInboundMessageStore() {
        return INBOUND_MESSAGE_STORE.getValue(properties);
    }

    public String getPersistenceClassPath() {
        return OXALIS_PERSISTENCE_CLASS_PATH.getValue(properties);
    }

    public boolean isSoapTraceEnabled() {
        return Boolean.valueOf(SOAP_TRACE.getValue(properties));
    }

    public String getInboundLoggingConfiguration() {
        String s = INBOUND_LOGGING_CONFIG.getValue(properties);
        return   s;
    }


    public PkiVersion getPkiVersion() {
        return PkiVersion.valueOf(PKI_VERSION.getValue(properties));
    }


    public OperationalMode getModeOfOperation() {
        return OperationalMode.valueOf(OPERATION_MODE.getValue(properties));
    }


    /**
     * Property definitions, which are declared separately from the actual instances of
     * the properties.
     */
    public static enum PropertyDef {
        /** Location of Java keystore holding our private key and signed certificate */
        KEYSTORE_PATH("oxalis.keystore", true),

        /** The password of the above keystore */
        KEYSTORE_PASSWORD("oxalis.keystore.password", true,"peppol"),

        /** Where to store inbound messages */
        INBOUND_MESSAGE_STORE("oxalis.inbound.message.store", true, System.getProperty("java.io.tmpdir") + "inbound"),

        /** Class path entry where the persistence module is located. */
        OXALIS_PERSISTENCE_CLASS_PATH("oxalis.persistence.class.path", false),

        /** Dump the SOAP HTTP traffic ? */
        SOAP_TRACE("oxalis.soap.trace", false, "false"),    // Default is off

        /** Name of JDBC Driver class */
        JDBC_DRIVER_CLASS("oxalis.jdbc.driver.class", true),

        /** The JDBC connection URL */
        JDBC_URI("oxalis.jdbc.connection.uri", true),

        /** JDBC User name */
        JDBC_USER("oxalis.jdbc.user", true),

        /** Jdbc password */
        JDBC_PASSWORD("oxalis.jdbc.password", true),

        /** Location of the JDBC driver named in JDBC_DRIVER_CLASS */
        JDBC_DRIVER_CLASS_PATH("oxalis.jdbc.class.path", true),

        /** Name of JNDI Data Source */
        JNDI_DATA_SOURCE("oxalis.datasource.jndi.name", false),

        /** Location of private RSA key used within the statistics module */
        STATISTICS_PRIVATE_KEY_PATH("oxalis.statistics.private.key", true),

        /**
         * Location of Logback configuration file for inbound server
         */
        INBOUND_LOGGING_CONFIG("oxalis.inbound.log.config", true, "logback-oxalis.xml"),

        /**
         * Location of Logback configuration file for standalone applications
         */
        APP_LOGGING_CONFIG("oxalis.app.log.config", false, "logback-oxalis.xml"),

        /** Oxalis statistics identifier */
        AP_ID("oxalis.ap.identifier",true),

        /**
         * PKI version to use
         */
        PKI_VERSION("oxalis.pki.version", true, PkiVersion.V1.name()),

        /**
         * Mode of operation, i.e. test or production. For PKI version 1, TEST is the only
         * mode available.
         */
        OPERATION_MODE("oxalis.operation.mode", true, OperationalMode.TEST.name()),
        ;

        /**
         * External name of property as it appears in your .properties file, i.e. with the dot notation,
         * like for instance "x.y.z = value"
         */
        private String propertyName;
        private boolean required;
        private final String defaultValue;

        /**
         * Enum constructor
         *
         * @param propertyName name of property as it appears in your .properties file
         */
        PropertyDef(String propertyName, boolean required) {
            this(propertyName, required,null);
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
                if (propertyValue == null)
                    return propertyValue.trim();
                else
                    return propertyValue;
            }
        }

        private String required(String value) {
            if (value == null || value.trim().length() == 0) {
                throw new IllegalStateException("Property '" + propertyName + "' does not exist or is empty, check " + OXALIS_GLOBAL_PROPERTIES);
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

        public String getPropertyName() {
            return propertyName;
        }
    }


}
