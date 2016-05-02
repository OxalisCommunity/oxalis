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

package eu.peppol.util;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 11.12.2015
 *         Time: 22.41
 */
public class UnitTestGlobalConfigurationImplTest {

    @Test
    public void createSampleInstance() throws Exception {
        GlobalConfiguration instance = UnitTestGlobalConfigurationImpl.createInstance();
        File oxalisHomeDir = instance.getOxalisHomeDir();

        assertNotNull(oxalisHomeDir, "Oxalis homedirectory is null");

        assertNotNull(instance.getKeyStoreFileName());
        File keystoreFile = new File(instance.getKeyStoreFileName());
        assertTrue(keystoreFile.exists());
        assertTrue(keystoreFile.isFile() && keystoreFile.canRead());

        assertTrue(instance.isTransmissionBuilderOverride(),"Transmission override should be alowed!");
    }
}