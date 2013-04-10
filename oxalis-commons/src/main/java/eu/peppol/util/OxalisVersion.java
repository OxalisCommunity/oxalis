package eu.peppol.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author steinar
 *         Date: 10.04.13
 *         Time: 10:42
 */
public class OxalisVersion {

    public static String getVersion() {

        InputStream inputStream = OxalisVersion.class.getClassLoader().getResourceAsStream("oxalis-version.properties");
        if (inputStream == null) {
            throw new IllegalStateException("Unable to locate resource oxalis.version in class path");
        }
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            return properties.getProperty("oxalis.version");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load data from resource oxalis.version");
        }

    }

}
