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

package eu.peppol.util;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.security.KeystoreLoader;
import eu.peppol.security.KeystoreManager;
import eu.peppol.security.KeystoreManagerImpl;

/**
 * @author steinar
 *         Date: 15.12.2015
 *         Time: 22.32
 */
public class OxalisCommonsTestModule extends AbstractModule
{

    @Override
    protected void configure() {
        // We load our key stores from the class path resources, which are included
        bind(KeystoreLoader.class).to(DummyKeystoreLoader.class).in(Singleton.class);

        bind(KeystoreManager.class).to(KeystoreManagerImpl.class).in(Singleton.class);

    }

    @Provides
    @Singleton
    public GlobalConfiguration provideGlobalConfiguration() {
        return UnitTestGlobalConfigurationImpl.createInstance();
    }
}
