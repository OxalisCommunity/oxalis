package eu.peppol.outbound;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import no.difi.vefa.peppol.lookup.LookupClient;
import org.mockito.Mockito;

import javax.inject.Singleton;

public class MockLookupModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Singleton
    @Provides
    LookupClient providesLookupClient() {
        return Mockito.mock(LookupClient.class);
    }
}
