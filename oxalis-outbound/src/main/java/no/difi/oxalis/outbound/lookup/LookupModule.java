/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.outbound.lookup;

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
        bind(Key.get(LookupService.class, Names.named("cached")))
                .to(CachedLookupService.class)
                .in(Singleton.class);

        bind(Key.get(LookupService.class, Names.named("default")))
                .to(DefaultLookupService.class)
                .in(Singleton.class);

        bind(MetadataFetcher.class)
                .to(OxalisApacheFetcher.class)
                .in(Singleton.class);
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
