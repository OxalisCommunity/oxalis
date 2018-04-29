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

package no.difi.oxalis.commons.filesystem.detector;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import no.difi.oxalis.api.filesystem.HomeDetector;
import no.difi.oxalis.api.util.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * @author erlend
 */
@Sort(3000)
public class EnvironmentHomeDetector implements HomeDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyHomeDetector.class);

    protected static final String OXALIS_HOME_VAR_NAME = "OXALIS_HOME";

    private Map<String, String> environment;

    @Inject
    public EnvironmentHomeDetector(@Named("environment") Map<String, String> environment) {
        this.environment = environment;
    }

    @Override
    public File detect() {
        if (!environment.containsKey(OXALIS_HOME_VAR_NAME))
            return null;

        String oxalis_home = environment.get(OXALIS_HOME_VAR_NAME);
        LOGGER.info("Using OXALIS_HOME specified as Environment Variable '{}' as '{}'.",
                OXALIS_HOME_VAR_NAME, oxalis_home);
        return new File(oxalis_home);
    }
}
