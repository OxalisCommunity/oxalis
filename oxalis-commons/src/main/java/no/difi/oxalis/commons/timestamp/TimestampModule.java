package no.difi.oxalis.commons.timestamp;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import no.difi.oxalis.api.timestamp.TimestampService;
import no.difi.vefa.peppol.mode.Mode;

public class TimestampModule extends AbstractModule {

    @Override
    protected void configure() {
        // Nothing here.
    }

    @Provides
    @Singleton
    TimestampService getTimestampService(Injector injector, Mode mode) {
        return injector.getProvider(
                Key.get(TimestampService.class, Names.named(mode.getString("timestamp")))).get();
    }

    @Provides
    @Singleton
    @Named("system")
    TimestampService getSystemTimestampService() {
        return new SystemTimestampService();
    }
}
