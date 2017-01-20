package no.difi.oxalis.commons.timestamp;

import com.google.inject.*;
import com.google.inject.name.Names;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.vefa.peppol.mode.Mode;

/**
 * Guice module making a default implementation of {@link TimestampProvider} available.
 * <p>
 * Available services (timestamp.service):
 * <ul>
 *  <li>system</li>
 * </ul>
 *
 * @author erlend
 * @since 4.0.0
 */
public class TimestampModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(TimestampProvider.class, Names.named("system")))
                .to(SystemTimestampProvider.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    protected TimestampProvider getTimestampService(Injector injector, Mode mode) {
        return injector.getProvider(
                Key.get(TimestampProvider.class, Names.named(mode.getString("timestamp.service")))).get();
    }
}
