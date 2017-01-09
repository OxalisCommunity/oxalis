package eu.peppol.outbound.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import no.difi.vefa.peppol.common.lang.PeppolLoadingException;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.LookupClientBuilder;
import no.difi.vefa.peppol.mode.Mode;

import javax.inject.Singleton;

public class LookupModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Singleton
    @Provides
    LookupClient providesLookupClient(Mode mode) throws PeppolLoadingException {
        return LookupClientBuilder.forMode(mode).build();
    }
}
