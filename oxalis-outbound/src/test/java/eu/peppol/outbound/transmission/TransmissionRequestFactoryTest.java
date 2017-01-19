package eu.peppol.outbound.transmission;

import eu.peppol.outbound.As2PrioritizedTransportModule;
import eu.peppol.outbound.lookup.LookupModule;
import eu.peppol.outbound.lookup.MockLookupModule;
import eu.peppol.outbound.guice.TestResourceModule;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;

@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class, TracingModule.class, ModeModule.class,
        LookupModule.class, As2PrioritizedTransportModule.class})
public class TransmissionRequestFactoryTest {

    @Inject
    private TransmissionRequestFactory transmissionRequestFactory;

    @Test
    public void simple() throws Exception {
        MockLookupModule.resetService();

        TransmissionRequest transmissionRequest;
        try (InputStream inputStream = getClass().getResourceAsStream("/simple-sbd.xml")) {
            transmissionRequest = transmissionRequestFactory.newInstance(inputStream);
        }

        Assert.assertNotNull(transmissionRequest.getHeader());
        Assert.assertNotNull(transmissionRequest.getEndpoint());
    }
}
