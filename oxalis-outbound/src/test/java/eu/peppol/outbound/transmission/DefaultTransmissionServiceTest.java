package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.outbound.MockLookupModule;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.TransmissionService;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import no.difi.oxalis.outbound.dummy.DummyModule;
import no.difi.oxalis.outbound.dummy.DummyTransmissionResponse;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;
import no.difi.vefa.peppol.lookup.LookupClient;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.net.URI;

@Guice(modules = {TransmissionTestModule.class, TransmissionModule.class, ModeModule.class, MockLookupModule.class, DummyModule.class, TracingModule.class})
public class DefaultTransmissionServiceTest {

    @Inject
    private LookupClient lookupClient;

    @Inject
    private TransmissionService transmissionService;

    @Test
    public void simple() throws Exception {
        Mockito.when(lookupClient.getEndpoint(Mockito.any(Header.class), Mockito.any(TransportProfile.class), Mockito.any(TransportProfile.class), Mockito.any(TransportProfile.class)))
                .thenReturn(Endpoint.of(null, TransportProfile.of("busdox-transport-dummy"), URI.create("http://localhost/"), null));

        TransmissionResponse transmissionResponse = transmissionService.send(getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml"));

        Assert.assertTrue(transmissionResponse instanceof DummyTransmissionResponse);
        Assert.assertEquals(transmissionResponse.getProtocol(), TransportProfile.of("busdox-transport-dummy"));

        Assert.assertNotNull(transmissionResponse.getHeader());
        Assert.assertNotNull(transmissionResponse.getProtocol());
    }
}
