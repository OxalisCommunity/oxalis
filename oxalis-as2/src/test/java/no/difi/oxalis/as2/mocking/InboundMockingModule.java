package no.difi.oxalis.as2.mocking;

import no.difi.oxalis.api.inbound.InboundService;
import no.difi.oxalis.commons.guice.OxalisModule;
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
