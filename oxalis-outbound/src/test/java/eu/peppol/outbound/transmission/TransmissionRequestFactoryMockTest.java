package eu.peppol.outbound.transmission;

import brave.Span;
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.outbound.lookup.MockLookupModule;
import eu.peppol.outbound.guice.TestResourceModule;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import no.difi.vefa.peppol.common.model.Header;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class, TracingModule.class, ModeModule.class, MockLookupModule.class})
public class TransmissionRequestFactoryMockTest {

    @Inject
    private TransmissionRequestFactory transmissionRequestFactory;

    @Inject
    private LookupService lookupService;

    @Test
    public void simple() throws Exception {
        MockLookupModule.resetService();

        TransmissionRequest transmissionRequest;
        try (InputStream inputStream = getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml")) {
            transmissionRequest = transmissionRequestFactory.newInstance(inputStream);
        }

        Assert.assertNotNull(transmissionRequest.getPeppolStandardBusinessHeader());
        Assert.assertNotNull(transmissionRequest.getEndpointAddress());
    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void endpintNotFound() throws Exception {
        Mockito.reset(lookupService);
        Mockito.when(lookupService.lookup(Mockito.any(Header.class), Mockito.any(Span.class)))
                .thenThrow(new OxalisTransmissionException("Exception from unit test."));

        try (InputStream inputStream = getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml")) {
            transmissionRequestFactory.newInstance(inputStream);
        }
    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void unrecognizedContent() throws Exception {
        transmissionRequestFactory.newInstance(new ByteArrayInputStream("Hello World!".getBytes()));
    }
}
