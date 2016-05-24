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

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 10.04.13
 *         Time: 10:50
 */
public class OxalisVersionTest {

    @Test
    public void testGetVersion() throws Exception {
        String currentVersion = OxalisVersion.getVersion();
        assertNotNull(currentVersion);
        System.out.printf("Current version is %s\n", currentVersion);
    }

    public void testGetBuildDescription() throws Exception {
        String buildDescription = OxalisVersion.getBuildDescription();
        assertNotNull(buildDescription);
        System.out.printf("Description is %s\n", buildDescription);
    }

}
