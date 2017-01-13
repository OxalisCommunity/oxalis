package eu.peppol.outbound.transmission;

import no.difi.oxalis.api.outbound.TransmissionRequest;
import eu.peppol.outbound.guice.TestResourceModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import eu.peppol.outbound.lookup.LookupModule;
import no.difi.oxalis.commons.mode.ModeModule;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;

@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class, TracingModule.class, ModeModule.class, LookupModule.class})
public class TransmissionRequestFactoryTest {

    @Inject
    private TransmissionRequestFactory transmissionRequestFactory;

    @Test
    public void simple() throws Exception {
        TransmissionRequest transmissionRequest;
        try (InputStream inputStream = getClass().getResourceAsStream("/simple-sbd.xml")) {
            transmissionRequest = transmissionRequestFactory.newInstance(inputStream);
        }

        Assert.assertNotNull(transmissionRequest.getPeppolStandardBusinessHeader());
        Assert.assertNotNull(transmissionRequest.getEndpointAddress());

        Thread.sleep(1000);
    }
}
