package eu.peppol.outbound.transmission;

import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.outbound.module.BraveTraceModule;
import eu.peppol.outbound.module.LookupModule;
import no.difi.oxalis.commons.module.ModeModule;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;

@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class, BraveTraceModule.class, ModeModule.class, LookupModule.class})
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
    }
}
