package no.difi.oxalis.inbound.persister;

import com.google.inject.*;
import com.google.inject.name.Names;
import no.difi.oxalis.api.inbound.ContentPersister;
import no.difi.oxalis.api.inbound.ReceiptPersister;

/**
 * @author erlend
 * @since 4.0.0
 */
public class PersisterModule extends AbstractModule {

    @Override
    protected void configure() {
        // Default
        bind(Key.get(ContentPersister.class, Names.named("default")))
                .to(DefaultPersister.class)
                .in(Singleton.class);
        bind(Key.get(ReceiptPersister.class, Names.named("default")))
                .to(DefaultPersister.class)
                .in(Singleton.class);

        // Noop
        bind(Key.get(ContentPersister.class, Names.named("noop")))
                .to(NoopPersister.class)
                .in(Singleton.class);
        bind(Key.get(ReceiptPersister.class, Names.named("noop")))
                .to(NoopPersister.class)
                .in(Singleton.class);

        // Temp
        bind(Key.get(ContentPersister.class, Names.named("temp")))
                .to(TempPersister.class)
                .in(Singleton.class);
        bind(Key.get(ReceiptPersister.class, Names.named("temp")))
                .to(TempPersister.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    protected ContentPersister getContentPersister(Injector injector) {
        return injector.getInstance(Key.get(ContentPersister.class, Names.named("default")));
    }

    @Provides
    @Singleton
    protected ReceiptPersister getReceiptPersister(Injector injector) {
        return injector.getInstance(Key.get(ReceiptPersister.class, Names.named("default")));
    }
}
