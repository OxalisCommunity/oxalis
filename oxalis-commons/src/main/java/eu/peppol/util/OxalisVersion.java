package eu.peppol.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides access to selected Maven injected properties in the oxalis-version.properties file.
 * @author steinar
 * @author thore
 */
public class OxalisVersion {

    private static Properties properties;

    static {
        InputStream inputStream = OxalisVersion.class.getClassLoader().getResourceAsStream("oxalis-version.properties");
        if (inputStream == null) {
            throw new IllegalStateException("Unable to locate resource oxalis.version in class path");
        }
        properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load data from resource oxalis.version");
        }
    }

    /**
     * The Oxalis version, taken from the POM
     */
    public static String getVersion() {
        return properties.getProperty("oxalis.version");
    }

    /**
     * The OS user (from environment) running the build
     */
    public static String getUser() {
        return properties.getProperty("oxalis.user");
    }

    /**
     * Describes the build SCM version
     */
    public static String getBuildDescription() {
        return properties.getProperty("git.commit.id.describe");
    }

    /**
     * Git SCM version, full format
     */
    public static String getBuildId() {
        return properties.getProperty("git.commit.id");
    }

    /**
     * The build commit time stamp
     */
    public static String getBuildTimeStamp() {
        return properties.getProperty("git.commit.time");
    }

}
