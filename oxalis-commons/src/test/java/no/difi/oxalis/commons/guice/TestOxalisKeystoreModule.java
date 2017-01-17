package no.difi.oxalis.commons.guice;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OxalisKeystoreModule;
import eu.peppol.util.UnitTestGlobalConfigurationImpl;

public class TestOxalisKeystoreModule extends OxalisKeystoreModule {

    @Provides
    @Singleton
    GlobalConfiguration provideTestConfiguration() {
        return UnitTestGlobalConfigurationImpl.createInstance();
    }
}
