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

package no.difi.oxalis.commons.filesystem.detector;

import eu.peppol.util.TestableInitialContextFactory;
import no.difi.oxalis.api.filesystem.HomeDetector;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import static no.difi.oxalis.commons.filesystem.detector.JndiHomeDetector.OXALIS_HOME_JNDI_PATH;

/**
 * @author erlend
 */
public class JndiHomeDetectorTest {

    private HomeDetector homeDetector = new JndiHomeDetector();

    @BeforeMethod
    public void clearSettingsForNextTest() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestableInitialContextFactory.class.getName());
        new InitialContext().unbind(OXALIS_HOME_JNDI_PATH);
    }

    @AfterMethod
    public void tearDown() throws NamingException {
        // Removes the JNDI entry for OXALIS_HOME
        new InitialContext().unbind(OXALIS_HOME_JNDI_PATH);
    }

    @Test
    public void testFromJndi() throws Exception {
        String path = new File("/some/system/path1").getAbsolutePath();
        File oxalis_home;

        //
        oxalis_home = homeDetector.detect();
        assertNull(oxalis_home);

        // bind value to JNDI and read
        new InitialContext().bind(OXALIS_HOME_JNDI_PATH, path);
        oxalis_home = homeDetector.detect();
        assertEquals(oxalis_home.getAbsolutePath(), path);
    }

}
