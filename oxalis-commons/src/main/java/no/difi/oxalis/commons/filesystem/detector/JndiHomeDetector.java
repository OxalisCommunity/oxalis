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

import no.difi.oxalis.api.filesystem.HomeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;

/**
 * @author erlend
 */
public class JndiHomeDetector implements HomeDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(JndiHomeDetector.class);

    public static final String OXALIS_HOME_JNDI_PATH = "java:comp/env/OXALIS_HOME";

    @Override
    public File detect() {
        try {
            String oxalis_home = (String) new InitialContext().lookup(OXALIS_HOME_JNDI_PATH);
            if (oxalis_home != null && oxalis_home.length() > 0) {
                LOGGER.info("Using OXALIS_HOME specified as JNDI path " + OXALIS_HOME_JNDI_PATH + " as " + oxalis_home);
                return new File(oxalis_home);
            }
        } catch (NamingException ex) {
            LOGGER.info("Unable to locate JNDI path " + OXALIS_HOME_JNDI_PATH + " ");
        }
        return null;
    }

}
