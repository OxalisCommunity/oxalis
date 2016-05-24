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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;

import static org.testng.Assert.*;

/**
 * @author steinar
 * @author thore
 */
public class OxalisHomeDirectoryTest {

    @BeforeMethod
    public void clearSettingsForNextTest() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestableInitialContextFactory.class.getName());
        new InitialContext().unbind(OxalisHomeDirectory.OXALIS_HOME_JNDI_PATH);
    }

    @AfterMethod
    public void tearDown() throws NamingException {
        // Removes the JNDI entry for OXALIS_HOME
        new InitialContext().unbind(OxalisHomeDirectory.OXALIS_HOME_JNDI_PATH);
    }

    @Test
    public void testFromJndi() throws Exception {

        String path = new File("/some/system/path1").getAbsolutePath();
        File oxalis_home = null;

        //
        oxalis_home = new OxalisHomeDirectory().locateOxalisHomeFromLocalJndiContext();
        assertNull(oxalis_home);

        // bind value to JNDI and read
        new InitialContext().bind(OxalisHomeDirectory.OXALIS_HOME_JNDI_PATH, path);
        oxalis_home = new OxalisHomeDirectory().locateOxalisHomeFromLocalJndiContext();
        assertEquals(oxalis_home.getAbsolutePath(), path);

    }

    @Test
    public void testFromJavaSystemProperty() {

        String path = new File("/some/system/path2").getAbsolutePath();
        String backup = System.getProperty(OxalisHomeDirectory.OXALIS_HOME_VAR_NAME);

        try {

            System.setProperty(OxalisHomeDirectory.OXALIS_HOME_VAR_NAME, "");
            File oxalis_home = new OxalisHomeDirectory().locateOxalisHomeFromJavaSystemProperty();
            assertNull(oxalis_home);

            System.setProperty(OxalisHomeDirectory.OXALIS_HOME_VAR_NAME, path);
            oxalis_home = new OxalisHomeDirectory().locateOxalisHomeFromJavaSystemProperty();
            assertEquals(oxalis_home.getAbsolutePath(), path);

        } finally {
            if (backup == null) backup = ""; // prevent null pointer exception
            System.setProperty(OxalisHomeDirectory.OXALIS_HOME_VAR_NAME, backup);
        }

    }

    @Test
    public void testFromEnvironmentVariable() {

        String path = System.getenv(OxalisHomeDirectory.OXALIS_HOME_VAR_NAME);
        File oxalis_home = new OxalisHomeDirectory().locateOxalisHomeFromEnvironmentVariable();

        // we cannot fake environment variables as they are in an UnmodifiableMap, only test when present
        if (path != null && path.length() > 0) {
            assertNotNull(oxalis_home);
            assertEquals(oxalis_home.getAbsolutePath(), path);
        } else {
            assertNull(oxalis_home);
        }

    }

    @Test(groups = {"integration"})
    public void makeSureWeHaveWorkingOxalisHomeDirectory() {

        File file = new OxalisHomeDirectory().locateDirectory();
        assertTrue(file.exists(), "OXALIS_HOME was not found");
        assertTrue(file.isDirectory(), "OXALIS_HOME was not a directory");
        assertTrue(file.canRead(), "OXALIS_HOME was not readable");

    }

}
