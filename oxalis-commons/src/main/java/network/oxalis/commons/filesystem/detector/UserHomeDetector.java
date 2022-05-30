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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author erlend
 */
@Slf4j
@Sort(4000)
@MetaInfServices
public class UserHomeDetector implements HomeDetector {

    @Override
    public File detect() {
        Path path = Paths.get(System.getProperty("user.home"), ".oxalis");
        if (Files.notExists(path))
            return null;

        log.info("Using Oxalis folder relative to home folder: {}", path);
        return path.toFile();
    }
}
