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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import network.oxalis.commons.config.ConfigModule;
import network.oxalis.test.filesystem.TestFileSystemModule;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;

public class FileSystemModuleTest {

    @Inject
    private Injector injector = Guice.createInjector(
            new ConfigModule(),
            Modules.override(new FileSystemModule()).with(new TestFileSystemModule()));

    @Test
    public void verifyHomeFolder() {
        Assert.assertNotNull(injector.getInstance(Key.get(Path.class, Names.named("home"))));
    }

    @Test
    public void verifyConfFolder() {
        Assert.assertNotNull(injector.getInstance(Key.get(Path.class, Names.named("conf"))));
    }

    @Test
    public void verifyInboundFolder() {
        Assert.assertNotNull(injector.getInstance(Key.get(Path.class, Names.named("inbound"))));
    }
}
