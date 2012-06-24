package eu.peppol.start.identifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import static eu.peppol.start.identifier.Configuration.PropertyDef.*;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 5:05:52 PM
 */
public final class Configuration {

    private static String CUSTOM_PROPERTIES_PATH = "/oxalis-web.properties";
    private static String FALLBACK_PROPERTIES_PATH = "/oxalis.properties";



    /**
     * Property definitions, which are declared separately from the actual instances of
     * the properties.
     */
    static enum PropertyDef {
        KEYSTORE_PATH("oxalis.keystore", true),
        KEYSTORE_PASSWORD("oxalis.keystore.password", true),
        INBOUND_MESSAGE_STORE("oxalis.inbound.message.store", false),
        OUTBOUND_MESSAGE_STORE("oxalis.outbound.message.store", false),
        WSDL_FILE_NAME("oxalis.wsdl", true),
        PEPPOL_SENDER_ID("peppol.senderid",true),
        PEPPOL_SERVICE_NAME("peppol.servicename",true),
        OXALIS_PERSISTENCE_CLASS_PATH("oxalis.persistence.class.path", false);


        /**
         * External name of property as it appears in your .properties file, i.e. with the dot notation,
         * like for instance "x.y.z = value"
         */
        private String propertyName;
        private boolean required;

        /**
         * Enum constructor
         *
         * @param propertyName name of property as it appears in your .properties file
         */
        PropertyDef(String propertyName, boolean required) {
            if (propertyName == null || propertyName.trim().length() == 0) {
                throw new IllegalArgumentException("Property name is required");
            }
            this.propertyName = propertyName;
            this.required = required;
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
            } else
                return properties.getProperty(propertyName);
        }

        String required(String value) {
            if (value == null || value.trim().length() == 0) {
                throw new IllegalStateException("Property '" + propertyName + "' does not exist or is empty, check " + FALLBACK_PROPERTIES_PATH);
            }
            return value;
        }


    }

    // Holds our singleton configuration instance
    private static Configuration instance;

    // Holds the properties, which we loaded upon instantiation
    private Properties properties;

    /**
     * This is the factory method, which gives access to the singleton instance
     * @return a reference to our singletion configuration
     */
    public static synchronized Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }

        return instance;
    }


    @SuppressWarnings("unchecked")
    private Configuration() {

        Properties fallBackProps = new Properties();
        loadPropertiesFile(fallBackProps, FALLBACK_PROPERTIES_PATH);

        Log.info("Loading properties from " + CUSTOM_PROPERTIES_PATH + ", which will fallback to " + FALLBACK_PROPERTIES_PATH + " if properties do not exist.");
        Properties customProps = new Properties(fallBackProps);
        loadPropertiesFile(customProps, CUSTOM_PROPERTIES_PATH);

        properties = customProps;

        Log.info("======= Properties in effect: =======");
        for (Enumeration<String> e = (Enumeration<String>) properties.propertyNames(); e.hasMoreElements();){
            String propName = e.nextElement();
            Log.info(propName + " = " + properties.getProperty(propName));
        }
        Log.info("======================================");
        Log.info("Configuration loaded.");
    }

    /**
     * Loads properties from the given properties resource into the given properties object.
     * @param properties references the object into which properties will be loaded
     * @param propertiesResourceName the name of the properties resource
     */
    void loadPropertiesFile(Properties properties, String propertiesResourceName) {
        URL url = getClass().getResource(propertiesResourceName);
        if (url == null) {
            Log.info("No properties loaded from " + propertiesResourceName + ", file was not found in classpath");
            return;
        }

        Log.info(propertiesResourceName + " --> " + url.toString());

        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("No configuration file found at " + FALLBACK_PROPERTIES_PATH, e);
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
                Log.warn("Unable to close input stream");
            }
        }
    }

    public String getKeyStoreFileName() {
        String s = KEYSTORE_PATH.getValue(properties);
        return s == null ? null : s.trim();
    }

    public String getKeyStorePassword() {
        return KEYSTORE_PASSWORD.getValue(properties);
    }

    public String getInboundMessageStore() {

        String msgStore = INBOUND_MESSAGE_STORE.getValue(properties);
        if (msgStore == null) {
            msgStore = System.getProperty("java.io.tmpdir") + File.separator + "inbound";
        }
        return msgStore;
    }

    public String getOutboundMessageStore() {
        String msgStore = OUTBOUND_MESSAGE_STORE.getValue(properties);
        if (msgStore == null) {
            msgStore = System.getProperty("java.io.tmpdir") + File.separator + "outbound";
        }
        return msgStore;
    }

    
    public String getWsdlFileName() {
        return WSDL_FILE_NAME.getValue(properties);
    }

    public String getPeppolSenderId() {
        return PEPPOL_SENDER_ID.getValue(properties);
    }

    public String getPeppolServiceName() {
        return PEPPOL_SERVICE_NAME.getValue(properties);
    }


    /** Provides the textual class path of the persistence API implementation.
     *
     * @return
     */
    public String getPersistenceClassPath() {
        String result = OXALIS_PERSISTENCE_CLASS_PATH.getValue(properties);
        if (result != null) {
            result = result.trim();
        }

        return result;
    }
}
