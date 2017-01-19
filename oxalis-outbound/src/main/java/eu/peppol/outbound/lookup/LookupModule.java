package eu.peppol.outbound.lookup;

import com.google.inject.*;
import com.google.inject.name.Names;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.vefa.peppol.common.lang.PeppolLoadingException;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.LookupClientBuilder;
import no.difi.vefa.peppol.lookup.api.MetadataFetcher;
import no.difi.vefa.peppol.mode.Mode;
import no.difi.vefa.peppol.security.api.CertificateValidator;

/**
 * @author erlend
 * @since 4.0.0
 */
public class LookupModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(LookupService.class, Names.named("cached"))).to(CachedLookupService.class).in(Singleton.class);
        bind(Key.get(LookupService.class, Names.named("default"))).to(DefaultLookupService.class).in(Singleton.class);

        bind(MetadataFetcher.class).to(OxalisApacheFetcher.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    protected LookupService getLookupService(Mode mode, Injector injector) {
        return injector.getInstance(Key.get(LookupService.class, Names.named(mode.getString("lookup.service"))));
    }

    @Provides
    @Singleton
    protected LookupClient providesLookupClient(Mode mode, CertificateValidator certificateValidator,
                                                MetadataFetcher fetcher)
            throws PeppolLoadingException {
        return LookupClientBuilder.forMode(mode)
                .fetcher(fetcher)
                .certificateValidator(certificateValidator)
                .build();
    }
}
