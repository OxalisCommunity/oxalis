package eu.peppol.outbound.transmission;

import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.outbound.As2PrioritizedTransportModule;
import eu.peppol.outbound.MockLookupModule;
import eu.peppol.outbound.guice.TestResourceModule;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import no.difi.vefa.peppol.common.lang.EndpointNotFoundException;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;
import no.difi.vefa.peppol.lookup.LookupClient;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class, TracingModule.class, ModeModule.class, MockLookupModule.class, As2PrioritizedTransportModule.class})
public class TransmissionRequestFactoryMockTest {

    @Inject
    private TransmissionRequestFactory transmissionRequestFactory;

    @Inject
    private LookupClient lookupClient;

    @Test
    public void simple() throws Exception {
        Mockito.reset(lookupClient);
        Mockito.when(lookupClient.getEndpoint(Mockito.any(Header.class), Mockito.any(TransportProfile.class)))
                .thenReturn(Endpoint.of(null, TransportProfile.AS2_1_0, URI.create("https://localhost/"), null));

        TransmissionRequest transmissionRequest;
        try (InputStream inputStream = getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml")) {
            transmissionRequest = transmissionRequestFactory.newInstance(inputStream);
        }

        Assert.assertNotNull(transmissionRequest.getPeppolStandardBusinessHeader());
        Assert.assertNotNull(transmissionRequest.getEndpointAddress());
    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void endpintNotFound() throws Exception {
        Mockito.reset(lookupClient);
        Mockito.when(lookupClient.getEndpoint(Mockito.any(Header.class), Mockito.any(TransportProfile.class)))
                .thenThrow(new EndpointNotFoundException("Exception from unit test."));

        try (InputStream inputStream = getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml")) {
            transmissionRequestFactory.newInstance(inputStream);
        }
    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void unrecognizedContent() throws Exception {
        transmissionRequestFactory.newInstance(new ByteArrayInputStream("Hello World!".getBytes()));
    }
}
