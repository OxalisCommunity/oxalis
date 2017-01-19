package eu.peppol.as2.outbound;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import no.difi.oxalis.api.outbound.MessageSender;

import javax.inject.Singleton;

/**
 * Guice module providing AS2 implementation for outbound.
 *
 * @author erlend
 * @since 4.0.0
 */
public class As2OutboundModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(MessageSender.class, Names.named("oxalis-as2")))
                .to(As2MessageSender.class)
                .in(Singleton.class);
    }
}
