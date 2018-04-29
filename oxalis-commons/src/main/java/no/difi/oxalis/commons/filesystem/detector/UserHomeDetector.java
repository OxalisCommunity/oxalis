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

import no.difi.oxalis.api.filesystem.HomeDetector;
import no.difi.oxalis.api.util.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author erlend
 */
@Sort(4000)
public class UserHomeDetector implements HomeDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHomeDetector.class);

    @Override
    public File detect() {
        Path path = Paths.get(System.getProperty("user.home"), ".oxalis");
        if (!Files.exists(path))
            return null;

        LOGGER.info("Using OXALIS_HOME relative to user.home as '{}'.", path);
        return path.toFile();
    }
}
