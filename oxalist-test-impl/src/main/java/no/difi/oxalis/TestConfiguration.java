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

package no.difi.oxalis;

import com.google.inject.Singleton;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.GlobalConfigurationImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a fake GlobalConfiguration instance, which works with our unit tests requiring access to an environment
 * in which a certificate is available.
 * <p>
 * Created by soc on 11.12.2015.
 */
@Singleton
public class TestConfiguration {


    public static final String FAKE_OXALIS_GLOBAL_PROPERTIES = "fake-oxalis-global.properties";
    private Path tempDirectory;


    public static GlobalConfiguration createInstance() {
        Path tempDirectory = null;
        try {
            tempDirectory = Files.createTempDirectory("unit-test");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new GlobalConfigurationImpl(tempDirectory.toFile(),testConfigProperties());
    }

    private TestConfiguration(){}

    static InputStream testConfigProperties() {

        InputStream resourceAsStream = TestConfiguration.class.getClassLoader().getResourceAsStream(FAKE_OXALIS_GLOBAL_PROPERTIES);
        if (resourceAsStream == null) {
            throw new IllegalStateException("Unable to locate " + FAKE_OXALIS_GLOBAL_PROPERTIES + " in classpath ");
        }
        return resourceAsStream;

    }
}
