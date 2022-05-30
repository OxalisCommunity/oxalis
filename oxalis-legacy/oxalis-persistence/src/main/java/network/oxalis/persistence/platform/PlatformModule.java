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

package network.oxalis.persistence.platform;

import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import network.oxalis.commons.guice.OxalisModule;
import network.oxalis.persistence.api.Platform;

/**
 * @author erlend
 */
public class PlatformModule extends OxalisModule {

    @Override
    protected void configure() {
        Multibinder<Platform> multibinder = Multibinder.newSetBinder(binder(), Platform.class);
        multibinder.addBinding().to(H2Platform.class);
        multibinder.addBinding().to(HSQLDBPlatform.class);
        multibinder.addBinding().to(MySQLPlatform.class);
        multibinder.addBinding().to(MsSQLPlatform.class);
        multibinder.addBinding().to(OraclePlatform.class);

        bind(Platform.class)
                .toProvider(PlatformProvider.class)
                .in(Singleton.class);
    }
}
