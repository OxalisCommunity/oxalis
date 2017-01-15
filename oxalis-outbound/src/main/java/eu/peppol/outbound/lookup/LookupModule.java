package eu.peppol.outbound.lookup;

import com.google.inject.*;
import com.google.inject.name.Names;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.vefa.peppol.common.lang.PeppolLoadingException;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.LookupClientBuilder;
import no.difi.vefa.peppol.mode.Mode;
import no.difi.vefa.peppol.security.api.CertificateValidator;


public class LookupModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(LookupService.class, Names.named("default"))).to(DefaultLookupService.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    LookupService getLookupService(Injector injector) {
        return injector.getInstance(Key.get(LookupService.class, Names.named("default")));
    }

    @Provides
    @Singleton
    LookupClient providesLookupClient(Mode mode, CertificateValidator certificateValidator) throws PeppolLoadingException {
        return LookupClientBuilder.forMode(mode)
                .certificateValidator(certificateValidator)
                .build();
    }
}
