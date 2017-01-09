package eu.peppol.outbound;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import no.difi.vefa.peppol.common.lang.PeppolLoadingException;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.LookupClientBuilder;
import no.difi.vefa.peppol.mode.Mode;
import org.mockito.Mockito;

public class TestLookupModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    LookupClient providesLookupClient() throws PeppolLoadingException {
        return Mockito.mock(LookupClient.class);
    }
}
