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

import java.util.Collections;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author erlend
 */
public class EnvironmentHomeDetectorTest {

    @Test
    public void notFound() {
        HomeDetector homeDetector = new EnvironmentHomeDetector(Collections.emptyMap());

        assertNull(homeDetector.detect());
    }

    @Test
    public void found() {
        HomeDetector homeDetector = new EnvironmentHomeDetector(
                Collections.singletonMap(EnvironmentHomeDetector.VARIABLE, "/tmp"));

        assertNotNull(homeDetector.detect());
    }
}
