package eu.peppol.util;

import eu.peppol.start.identifier.AccessPointIdentifier;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Global configuration of Oxalis to be used by both stand alone and web components.
 * With this class, the conecpt of an Oxalis home directory is introduced.
 *
 * <p>See {@link OxalisHomeDirectory} for a description on how the Oxalis home directory is located.</p>
 *
 * User: steinar
 * Date: 08.02.13
 * Time: 12:45
 */
public enum GlobalConfiguration {

    INSTANCE;

    Properties properties;
    private final File oxalisGlobalPropertiesFileName;

    public static GlobalConfiguration getInstance() {
        return INSTANCE;
    }

    GlobalConfiguration() {

        properties = new Properties();
        File oxalisHomeDirectory = new OxalisHomeDirectory().locateDirectory();
        oxalisGlobalPropertiesFileName = new File(oxalisHomeDirectory, "oxalis-global.properties");
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(oxalisGlobalPropertiesFileName), Charset.forName("UTF-8"));
            properties.load(inputStreamReader);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to open " + oxalisGlobalPropertiesFileName + "; " + e, e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read from " + oxalisGlobalPropertiesFileName + "; " + e, e);
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to close file " + oxalisGlobalPropertiesFileName);
                }
            }
        }
    }

    public String getJdbcDriverClassName() {
        String propName = "oxalis.jdbc.driver.class";
        return getPropertyValue(propName);
    }


    private String getPropertyValue(String propName) {
        String value;
        if ((value=properties.getProperty(propName)) == null) {
            throw new IllegalArgumentException("Property " + propName + " not found in configuration file " + oxalisGlobalPropertiesFileName);
        } else
            return value;
    }

    public String getConnectionURI() {
        return getPropertyValue("oxalis.jdbc.connection.uri");
    }

    public String getUserName() {
        return getPropertyValue("oxalis.jdbc.user");
    }

    public String getPassword() {
        return getPropertyValue("oxalis.jdbc.password");
    }

    public AccessPointIdentifier getAccessPointIdentifier() {
        return new AccessPointIdentifier(getPropertyValue("oxalis.ap.identifier"));  //To change body of created methods use File | Settings | File Templates.
    }

    public String getDataSourceJndiName() {
        return getPropertyValue("oxalis.datasource.jndi.name");
    }
}
