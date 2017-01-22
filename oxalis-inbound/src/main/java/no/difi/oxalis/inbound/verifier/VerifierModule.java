package no.difi.oxalis.inbound.verifier;

import com.google.inject.*;
import com.google.inject.name.Names;
import no.difi.oxalis.api.inbound.InboundVerifier;

/**
 * @author erlend
 * @since 4.0.0
 */
public class VerifierModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(InboundVerifier.class, Names.named("default")))
                .to(DefaultVerifier.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    protected InboundVerifier getInboundVerifier(Injector injector) {
        return injector.getInstance(Key.get(InboundVerifier.class, Names.named("default")));
    }
}
