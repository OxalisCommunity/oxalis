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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides a fake GlobalConfiguration instance, which works with our unit tests requiring access to an environment
 * in which a certificate is available.
 * <p>
 * Created by soc on 11.12.2015.
 */
@Singleton
public class GlobalTestConfigurationImpl extends GlobalConfigurationImpl implements GlobalConfiguration{

    public static final String FAKE_OXALIS_GLOBAL_PROPERTIES = "fake-oxalis-global.properties";

    public GlobalTestConfigurationImpl() {
        super();
    }

    @Override
    protected File computeAndSetOxalisHomeDirectory() {
        String tmpDirPath = System.getProperty("java.io.tmpdir");
        System.out.println("Computing Oxalis home directory, " + tmpDirPath);

        try {
            Path homeDir = Files.createDirectories(Paths.get(tmpDirPath, "oxalis-home"));
            return homeDir.toFile();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to crate temp path for oxalis home " + e.getMessage(), e);

        }
    }

    @Override
    protected File computeOxalisGlobalPropertiesFileName(File homeDir) {

        assert homeDir != null : "Oxalis Home dir has not been computed correctly";

        File file = new File(homeDir, FAKE_OXALIS_GLOBAL_PROPERTIES);
        return file;
    }


    @Override
    protected void loadPropertiesFromFile() {
    }

    @Override
    protected void modifyProperties() {
        super.modifyProperties();
        // Sets the path to our dummy keystore
        getProperties().setProperty(PropertyDef.KEYSTORE_PATH.getPropertyName(), new File(getOxalisHomeDir(), "security/oxalis-dummy-keystore.jks").toString());

    }
}
