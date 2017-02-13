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
import no.difi.oxalis.api.util.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author erlend
 */
@Sort(2000)
public class PropertyHomeDetector implements HomeDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyHomeDetector.class);

    protected static final String OXALIS_HOME_VAR_NAME = "OXALIS_HOME";

    @Override
    public File detect() {
        String oxalis_home = System.getProperty(OXALIS_HOME_VAR_NAME);
        if (oxalis_home == null || oxalis_home.isEmpty())
            return null;

        LOGGER.info("Using OXALIS_HOME specified as Java System Property '-D {}' as '{}'.",
                OXALIS_HOME_VAR_NAME, oxalis_home);
        return new File(oxalis_home);
    }
}
