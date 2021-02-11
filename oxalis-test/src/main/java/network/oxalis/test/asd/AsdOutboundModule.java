package network.oxalis.test.asd;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import network.oxalis.api.outbound.MessageSender;

/**
 * @author erlend
 */
public class AsdOutboundModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(MessageSender.class, Names.named("oxalis-asd")))
                .to(AsdMessageSender.class)
                .in(Singleton.class);
    }
}
