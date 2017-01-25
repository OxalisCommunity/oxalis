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

package eu.peppol.outbound.lookup;

import brave.Span;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.oxalis.test.security.CertificateMock;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.mockito.Mockito;

import javax.inject.Singleton;
import java.net.URI;

public class MockLookupModule extends AbstractModule {

    private static LookupService lookupService = Mockito.mock(LookupService.class);

    public static void resetService() {
        try {
            Endpoint endpoint = Endpoint.of(TransportProfile.of("busdox-transport-dummy"), URI.create("http://localhost/"), CertificateMock.withCN("APP_00000042"));

            Mockito.reset(lookupService);
            Mockito.when(lookupService.lookup(Mockito.any(Header.class))).thenReturn(endpoint);
            Mockito.when(lookupService.lookup(Mockito.any(Header.class), Mockito.any(Span.class))).thenReturn(endpoint);

        } catch (OxalisTransmissionException e) {
            // No action
        }
    }

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    LookupService providesLookupService() {
        return lookupService;
    }
}
