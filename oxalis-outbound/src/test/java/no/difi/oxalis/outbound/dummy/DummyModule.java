package no.difi.oxalis.outbound.dummy;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import no.difi.oxalis.api.outbound.MessageSender;

public class DummyModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(MessageSender.class, Names.named("dummy"))).to(DummyMessageSender.class).in(Singleton.class);
    }
}
