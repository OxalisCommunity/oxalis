package eu.peppol.start.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import static eu.peppol.start.util.Configuration.PropertyDef.*;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 5:05:52 PM
 */
public final class Configuration {

    private static String PROPERTIES_PATH = "/oxalis.properties";


    /**
     * Property definitions, which are declared separately from the actual instances of
     * the properties.
     */
    static enum PropertyDef {
        KEYSTORE_PATH ("oxalis.keystore"),
        KEYSTORE_PASSWORD("oxalis.keystore.password");

        /**
         * External name of property as it appears in your .properties file, i.e. with the dot notation,
         * like for instance "x.y.z = value"
         * @return external name of property
         */
        public String getPropertyName() {
            return propertyName;
        }

        private String propertyName;

        /**
         * Enum constructor
         * @param propertyName name of property as it appears in your .properties file
         */
        PropertyDef(String propertyName) {
            if (propertyName == null || propertyName.trim().length() == 0) {
                throw new IllegalArgumentException("Property name is required");
            }
            this.propertyName = propertyName;
        }

        /**
         * Locates the value of this named property in the supplied collection of properties.
         * @param properties collection of properties to search
         * @return value of property
         */
        public String getValue(Properties properties) {
            return required(properties.getProperty(propertyName));
        }

        String required(String value) {
            if (value == null || value.trim().length() == 0) {
                throw new IllegalStateException("Property '" + propertyName + "' does not exist or is empty, check " + PROPERTIES_PATH);
            }
            return value;
        }


    }

    // Holds our singleton configuration instance
    private static Configuration instance;

    // Holds the properties, which we loaded upon instantiation
    private Properties properties = new Properties();

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

    private Configuration() {
        InputStream inputStream = getClass().getResourceAsStream(PROPERTIES_PATH);

        try {

            properties.load(inputStream);

        } catch (IOException e) {
            throw new RuntimeException("No configuration file found at " + PROPERTIES_PATH, e);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                Log.warn("Unable to close input stream");
            }
        }

        Log.debug("Configuration loaded from " + PROPERTIES_PATH);
    }

    @Deprecated
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getKeystoreFilename() {
        return KEYSTORE_PATH.getValue(properties);
    }

    public String getKeyStorePassword() {
        return KEYSTORE_PASSWORD.getValue(properties);
    }

}
