package no.difi.oxalis.inbound.persister;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import no.difi.oxalis.api.inbound.PayloadPersister;
import no.difi.oxalis.api.inbound.ReceiptPersister;

/**
 * @author erlend
 * @since 4.0.0
 */
public class PersisterModule extends AbstractModule {

    @Override
    protected void configure() {
        // Default
        bind(Key.get(PayloadPersister.class, Names.named("default")))
                .to(DefaultPersister.class)
                .in(Singleton.class);
        bind(Key.get(ReceiptPersister.class, Names.named("default")))
                .to(DefaultPersister.class)
                .in(Singleton.class);

        // Noop
        bind(Key.get(PayloadPersister.class, Names.named("noop")))
                .to(NoopPersister.class)
                .in(Singleton.class);
        bind(Key.get(ReceiptPersister.class, Names.named("noop")))
                .to(NoopPersister.class)
                .in(Singleton.class);

        // Temp
        bind(Key.get(PayloadPersister.class, Names.named("temp")))
                .to(TempPersister.class)
                .in(Singleton.class);
        bind(Key.get(ReceiptPersister.class, Names.named("temp")))
                .to(TempPersister.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    protected PayloadPersister getPayloadPersister(Injector injector, Config config) {
        return injector.getInstance(Key.get(PayloadPersister.class,
                Names.named(config.getString("persister.payload.service"))));
    }

    @Provides
    @Singleton
    protected ReceiptPersister getReceiptPersister(Injector injector, Config config) {
        return injector.getInstance(Key.get(ReceiptPersister.class,
                Names.named(config.getString("persister.receipt.service"))));
    }
}
