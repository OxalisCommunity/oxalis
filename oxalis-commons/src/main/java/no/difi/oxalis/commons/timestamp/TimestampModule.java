package no.difi.oxalis.commons.timestamp;

import com.google.inject.*;
import com.google.inject.name.Names;
import no.difi.oxalis.api.timestamp.TimestampService;
import no.difi.vefa.peppol.mode.Mode;

/**
 * Guice module making a default implementation of {@link TimestampService} available.
 * <p>
 * Available services (timestamp.service):
 * <ul>
 *  <li>system</li>
 * </ul>
 */
public class TimestampModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(TimestampService.class, Names.named("system")))
                .to(SystemTimestampService.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    TimestampService getTimestampService(Injector injector, Mode mode) {
        return injector.getProvider(
                Key.get(TimestampService.class, Names.named(mode.getString("timestamp.service")))).get();
    }
}
