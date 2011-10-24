package eu.peppol.start.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 5:05:52 PM
 */
public final class Configuration {

    private static String PROPERTIES_PATH = "/oxalis.properties";
    private static Configuration instance;

    private Properties properties = new Properties();

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
            }
        }

        Log.info("");
        Log.info("Configuration loaded from " + PROPERTIES_PATH);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
