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
