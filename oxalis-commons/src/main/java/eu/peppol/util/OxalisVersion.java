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

package eu.peppol.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Provides access to selected Maven injected properties in the oxalis-version.properties file.
 * @author steinar
 * @author thore
 */
public class OxalisVersion {

    public static final Logger log = LoggerFactory.getLogger(OxalisVersion.class);

    private static Properties properties;

    static {
        URL url = OxalisVersion.class.getClassLoader().getResource("oxalis-version.properties");
        log.debug("Loading oxalis-version.properties from: " + url.toString());

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
