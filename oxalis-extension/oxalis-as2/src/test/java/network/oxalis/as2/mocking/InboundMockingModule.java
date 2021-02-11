package network.oxalis.as2.mocking;

import network.oxalis.api.inbound.InboundService;
import network.oxalis.commons.guice.OxalisModule;
import org.mockito.Mockito;

/**
 * @author erlend
 */
public class InboundMockingModule extends OxalisModule {

    @Override
    protected void configure() {
        bind(InboundService.class).toInstance(Mockito.mock(InboundService.class));
    }
}
