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

package network.oxalis.commons.util;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 * Date: 10.04.13
 * Time: 10:50
 */
@Slf4j
public class OxalisVersionTest {

    @Test
    public void simpleConstructor() {
        new OxalisVersion();
    }

    @Test
    public void simple() {
        assertNotNull(OxalisVersion.getVersion());
        assertNotNull(OxalisVersion.getUser());
        assertNotNull(OxalisVersion.getBuildDescription());
        assertNotNull(OxalisVersion.getBuildId());
        assertNotNull(OxalisVersion.getBuildTimeStamp());
    }

    @Test
    public void testGetVersion() throws Exception {
        String currentVersion = OxalisVersion.getVersion();
        assertNotNull(currentVersion);
        log.info("Current version is '{}'.", currentVersion);
    }

    @Test
    public void testGetBuildDescription() throws Exception {
        String buildDescription = OxalisVersion.getBuildDescription();
        assertNotNull(buildDescription);
        log.info("Description is '{}'.", buildDescription);
    }

    @Test
    public void testBuildTimeStamp() {
        String buildTimeStamp = OxalisVersion.getBuildTimeStamp();
        assertNotNull(buildTimeStamp, "Build time stamp not set");
        log.info("Build time stamp is '{}'.", buildTimeStamp);
    }
}
