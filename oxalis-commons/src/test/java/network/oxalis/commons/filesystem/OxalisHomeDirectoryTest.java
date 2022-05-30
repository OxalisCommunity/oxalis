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

package network.oxalis.commons.filesystem;

import com.google.inject.Inject;
import network.oxalis.api.lang.OxalisLoadingException;
import network.oxalis.commons.guice.GuiceModuleLoader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;

/**
 * @author steinar
 * @author thore
 * @author erlend
 */
@Guice(modules = GuiceModuleLoader.class)
public class OxalisHomeDirectoryTest {

    @Inject
    private OxalisHomeDirectory oxalisHomeDirectory;

    private Path fakeHome;

    @BeforeClass
    public void beforeClass() throws Exception {
        this.fakeHome = new File(getClass().getResource("/oxalis_home/fake-oxalis.conf").toURI())
                .toPath().getParent();
    }

    @Test
    public void simple() {
        Assert.assertNotNull(oxalisHomeDirectory);
    }

    @Test
    public void mockingFound() {
        OxalisHomeDirectory oxalisHomeDirectory = new OxalisHomeDirectory(
                Collections.singletonList(() -> fakeHome.toFile()));

        Assert.assertNotNull(oxalisHomeDirectory.detect());
    }

    @Test(expectedExceptions = OxalisLoadingException.class)
    public void mockingNotFound() {
        OxalisHomeDirectory oxalisHomeDirectory = new OxalisHomeDirectory(
                Collections.singletonList(() -> null));
        oxalisHomeDirectory.detect();
    }

    @Test(expectedExceptions = OxalisLoadingException.class)
    public void mockingInvalid() {
        OxalisHomeDirectory oxalisHomeDirectory = new OxalisHomeDirectory(
                Collections.singletonList(() -> new File("/invalid")));
        oxalisHomeDirectory.detect();
    }

    @Test(expectedExceptions = OxalisLoadingException.class)
    public void mockingFile() {
        OxalisHomeDirectory oxalisHomeDirectory = new OxalisHomeDirectory(
                Collections.singletonList(() -> fakeHome.resolve("fake-oxalis.conf").toFile()));
        oxalisHomeDirectory.detect();
    }
}
