package eu.peppol.outbound.transmission;

import eu.peppol.outbound.MockLookupModule;
import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.tracing.TracingModule;
import no.difi.oxalis.commons.module.ModeModule;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.ProcessIdentifier;
import no.difi.vefa.peppol.common.model.TransportProfile;
import no.difi.vefa.peppol.lookup.LookupClient;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;
import java.io.InputStream;
import java.security.cert.X509Certificate;

@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class, TracingModule.class, ModeModule.class, MockLookupModule.class})
public class TransmissionRequestFactoryMockTest {

    @Inject
    private TransmissionRequestFactory transmissionRequestFactory;

    @Inject
    private LookupClient lookupClient;

    @Test
    public void simple() throws Exception {
        X509Certificate certificate = Mockito.mock(X509Certificate.class);
        Mockito.when(certificate.getSubjectX500Principal())
                .thenReturn(new X500Principal("CN=APP_1000000005,O=DIFI,C=NO"));
        Mockito.when(lookupClient.getEndpoint(Mockito.any(Header.class), Mockito.any(TransportProfile.class)))
                .thenReturn(Endpoint.of(
                        ProcessIdentifier.of("urn:www.cenbii.eu:profile:bii04:ver1.0"),
                        TransportProfile.AS2_1_0,
                        "https://test-aksesspunkt.difi.no/",
                        certificate
                ));

        TransmissionRequest transmissionRequest;
        try (InputStream inputStream = getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml")) {
            transmissionRequest = transmissionRequestFactory.newInstance(inputStream);
        }

        Assert.assertNotNull(transmissionRequest.getPeppolStandardBusinessHeader());
        Assert.assertNotNull(transmissionRequest.getEndpointAddress());

        Thread.sleep(1000);
    }
}
