/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.outbound.lookup;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import network.oxalis.api.lookup.LookupService;
import network.oxalis.commons.guice.OxalisModule;
import network.oxalis.commons.mode.OxalisCertificateValidator;
import network.oxalis.vefa.peppol.common.lang.PeppolLoadingException;
import network.oxalis.vefa.peppol.lookup.LookupClient;
import network.oxalis.vefa.peppol.lookup.LookupClientBuilder;
import network.oxalis.vefa.peppol.lookup.api.MetadataFetcher;
import network.oxalis.vefa.peppol.mode.Mode;

/**
 * @author erlend
 * @since 4.0.0
 */
public class LookupModule extends OxalisModule {

    @Override
    protected void configure() {
        bindTyped(LookupService.class, CachedLookupService.class);
        bindTyped(LookupService.class, DefaultLookupService.class);

        bind(MetadataFetcher.class)
                .to(OxalisApacheFetcher.class);
    }

    @Provides
    @Singleton
    protected LookupService getLookupService(Mode mode, Injector injector) {
        return injector.getInstance(Key.get(LookupService.class, Names.named(mode.getString("oxalis.lookup.service"))));
    }

    @Provides
    @Singleton
    protected LookupClient providesLookupClient(Mode mode, OxalisCertificateValidator certificateValidator,
                                                MetadataFetcher fetcher)
            throws PeppolLoadingException {
        return LookupClientBuilder.forMode(mode)
                .fetcher(fetcher)
                .certificateValidator(certificateValidator)
                .build();
    }
}
