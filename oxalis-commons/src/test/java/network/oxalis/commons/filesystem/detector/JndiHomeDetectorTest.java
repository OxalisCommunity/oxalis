/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.commons.filesystem.detector;

import network.oxalis.api.filesystem.HomeDetector;
import org.testng.annotations.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author erlend
 */
public class JndiHomeDetectorTest {

    private HomeDetector homeDetector = new JndiHomeDetector();

    @Test
    public void testFromJndi() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestableInitialContextFactory.class.getName());
        new InitialContext().unbind(JndiHomeDetector.VARIABLE);

        String path = new File("/some/system/path1").getAbsolutePath();

        //
        File oxalis_home = homeDetector.detect();
        assertNull(oxalis_home);

        // bind value to JNDI and read
        new InitialContext().bind(JndiHomeDetector.VARIABLE, path);
        oxalis_home = homeDetector.detect();
        assertEquals(oxalis_home.getAbsolutePath(), path);

        // Removes the JNDI entry for OXALIS_HOME
        new InitialContext().unbind(JndiHomeDetector.VARIABLE);
    }

    @Test
    public void testEmpty() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestableInitialContextFactory.class.getName());
        new InitialContext().unbind(JndiHomeDetector.VARIABLE);

        File oxalis_home = homeDetector.detect();
        assertNull(oxalis_home);

        // bind value to JNDI and read
        new InitialContext().bind(JndiHomeDetector.VARIABLE, "");
        assertNull(homeDetector.detect());

        // Removes the JNDI entry for OXALIS_HOME
        new InitialContext().unbind(JndiHomeDetector.VARIABLE);
    }

    @Test
    public void testNotSet() throws Exception {
        System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);

        assertNull(homeDetector.detect());
    }
}
