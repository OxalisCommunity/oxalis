package eu.peppol.as2.outbound;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import no.difi.oxalis.api.outbound.MessageSender;

import javax.inject.Singleton;

public class As2OutboundModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(MessageSender.class, Names.named("oxalis-as2"))).to(As2MessageSender.class).in(Singleton.class);
    }
}
