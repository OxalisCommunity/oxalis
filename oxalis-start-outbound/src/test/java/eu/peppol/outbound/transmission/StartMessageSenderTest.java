package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 05.11.13
 *         Time: 14:09
 */


@Guice(modules = {TransmissionTestModule.class})
@Test(groups = {"integration"})
public class StartMessageSenderTest {


    @Inject
    StartMessageSender startMessageSender;


    @Inject TransmissionRequestBuilder transmissionRequestBuilder;


    @Test
    public void testSend() throws Exception {

        InputStream inputStream = StartMessageSenderTest.class.getClassLoader().getResourceAsStream("ehf-t10-alle-elementer.xml");
        assertNotNull(startMessageSender);
        assertNotNull(inputStream);

        String url = "https://ap.unit4.com/oxalis/accessPointService";
        TransmissionRequest transmissionRequest = transmissionRequestBuilder.payLoad(inputStream).endPoint(new URL(url), BusDoxProtocol.START).build();

        assertEquals(transmissionRequest.getEndpointAddress().getUrl(), new URL(url));
        TransmissionResponse response = startMessageSender.send(transmissionRequest);


    }
}
