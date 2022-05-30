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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author erlend
 */
public class UserHomeDetectorTest {

    private HomeDetector homeDetector = new UserHomeDetector();

    private String backup;

    @BeforeClass
    public void beforeClass() {
        backup = System.getProperty("user.home");
    }

    @AfterClass
    public void afterClass() {
        System.setProperty("user.home", backup);
    }

    @Test
    public void valid() throws Exception {
        Path path = Paths.get(getClass().getResource("/.oxalis").toURI()).getParent();
        System.setProperty("user.home", path.toAbsolutePath().toString());

        Assert.assertNotNull(homeDetector.detect());
    }

    @Test
    public void invalid() {
        System.setProperty("user.home", "/invalid");

        Assert.assertNull(homeDetector.detect());
    }
}
