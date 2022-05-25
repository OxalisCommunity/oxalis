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

import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.filesystem.HomeDetector;
import network.oxalis.api.util.Sort;
import org.kohsuke.MetaInfServices;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;

/**
 * @author erlend
 */
@Slf4j
@Sort(1000)
@MetaInfServices
public class JndiHomeDetector implements HomeDetector {

    protected static final String VARIABLE = "java:comp/env/OXALIS_HOME";

    @Override
    public File detect() {
        try {
            String value = (String) new InitialContext().lookup(VARIABLE);
            if (value == null || value.isEmpty())
                return null;

            log.info("Using Oxalis folder specified as JNDI path '{}' with value '{}'.",
                    VARIABLE, value);
            return new File(value);
        } catch (NamingException ex) {
            return null;
        }
    }
}
