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

package network.oxalis.test.filesystem;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import network.oxalis.api.lang.OxalisLoadingException;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author erlend
 */
public class TestFileSystemModule extends AbstractModule {

    @Override
    protected void configure() {
        // No action
    }

    @Provides
    @Singleton
    @Named("home")
    protected Path getHomeFolder() {
        try {
            return Paths.get(getClass().getResource("/oxalis_home/fake-oxalis.conf").toURI()).getParent();
        } catch (URISyntaxException e) {
            throw new OxalisLoadingException(e.getMessage(), e);
        }
    }
}
