package no.difi.oxalis.commons.error;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import no.difi.oxalis.api.error.ErrorTracker;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.commons.guice.ImplLoader;
import no.difi.oxalis.commons.guice.OxalisModule;

/**
 * @author erlend
 * @since 4.0.2
 */
public class ErrorModule extends OxalisModule {

    @Override
    protected void configure() {
        bindTyped(ErrorTracker.class, NoopErrorTracker.class);

        bindSettings(ErrorConf.class);
    }

    @Provides
    @Singleton
    protected ErrorTracker getErrorTracker(Injector injector, Settings<ErrorConf> settings) {
        return ImplLoader.get(injector, ErrorTracker.class, settings, ErrorConf.TRACKER);
    }
}
