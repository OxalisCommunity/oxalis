package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.util.Util;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 04.11.13
 *         Time: 10:05
 */
@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class})
public class TransmissionRequestBuilderTest {

    @Inject
    TransmissionRequestBuilder transmissionRequestBuilder;

    @Inject @Named("sampleXml")
    InputStream inputStream;

    @BeforeMethod
    public void setUp() {
        inputStream.mark(Integer.MAX_VALUE);
    }

    @AfterMethod
    public void tearDown() throws IOException {
        inputStream.reset();
    }

    @Test
    public void createTransmissionRequestBuilderWithOnlyTheMessageDocument() throws Exception {

        assertNotNull(transmissionRequestBuilder);
        assertNotNull(inputStream);
        assertNotNull(transmissionRequestBuilder.sbdhParser);


        transmissionRequestBuilder.payLoad(inputStream);
        PeppolStandardBusinessHeader sbdh = transmissionRequestBuilder.getPeppolStandardBusinessHeader();
        assertNotNull(sbdh);
        assertEquals(sbdh.getRecipientId(), WellKnownParticipant.U4_TEST);

        // Builds the actual transmission request
        TransmissionRequest transmissionRequest = transmissionRequestBuilder.build();

        assertNotNull(transmissionRequest.getEndpointAddress());

        assertNotNull(transmissionRequest.getPeppolStandardBusinessHeader());

        assertEquals(transmissionRequest.getPeppolStandardBusinessHeader().getRecipientId(), WellKnownParticipant.U4_TEST);

        assertEquals(transmissionRequest.getEndpointAddress().getBusDoxProtocol(), BusDoxProtocol.AS2);

    }

    @Test
    public void testOverrideEndPoint() throws Exception {
        assertNotNull(inputStream);

        URL url = new URL("http://localhost:8080/oxalis/as2");
        TransmissionRequest request = transmissionRequestBuilder.payLoad(inputStream).endPoint(url, BusDoxProtocol.AS2).build();

        assertEquals(request.getEndpointAddress().getBusDoxProtocol(), BusDoxProtocol.AS2);
        assertEquals(request.getEndpointAddress().getUrl(), url);
    }
}
