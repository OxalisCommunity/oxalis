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

package no.difi.oxalis.commons.filesystem;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * @author erlend
 * @since 4.0.0
 */
public class FileSystemModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(FileSystem.class, Names.named("default")))
                .toInstance(FileSystems.getDefault());
    }

    @Provides
    @Singleton
    protected FileSystem getFileSystem(Injector injector) {
        return injector.getInstance(Key.get(FileSystem.class, Names.named("default")));
    }

    @Provides
    @Singleton
    @Named("home")
    protected Path getHomeFolder() {
        return OxalisHomeDirectory.locateDirectory().toPath();
    }
}
