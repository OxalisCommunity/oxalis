package eu.peppol.timestamp;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import no.difi.oxalis.api.timestamp.TimestampService;

public class TimestampModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    TimestampService getTimestampService(Injector injector) {
        return injector.getProvider(Key.get(TimestampService.class, Names.named("system"))).get();
    }

    @Provides
    @Singleton
    @Named("system")
    TimestampService getSystemTimestampService() {
        return new SystemTimestampService();
    }
}
