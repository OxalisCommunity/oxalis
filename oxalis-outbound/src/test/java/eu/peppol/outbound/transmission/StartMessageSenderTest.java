package eu.peppol.outbound.transmission;

import com.google.inject.Injector;
import com.google.inject.Stage;
import eu.peppol.util.GlobalState;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Attempts to send a message to the UNIT4 access point using the START message sender.
 *
 * Purpose of this test is to verify that the implementation of the START protocol works as expected after
 * refactoring.
 *
 * @author steinar
 *         Date: 05.11.13
 *         Time: 14:09
 */
@Test(groups = {"manual"})
public class StartMessageSenderTest {

    StartMessageSender startMessageSender;
    TransmissionRequestBuilder transmissionRequestBuilder;

    @BeforeMethod
    public void setUp() {
        GlobalState.getInstance().setTransmissionBuilderOverride(true);
        Injector injector = com.google.inject.Guice.createInjector(Stage.DEVELOPMENT, new TransmissionTestModule());
        startMessageSender = injector.getInstance(StartMessageSender.class);
        transmissionRequestBuilder = injector.getInstance(TransmissionRequestBuilder.class);
    }

    @Test
    public void sendSampleEhfToUnit4() throws Exception {
        InputStream inputStream = StartMessageSenderTest.class.getClassLoader().getResourceAsStream("ehf-bii05-t10-valid-invoice.xml");
        assertNotNull(startMessageSender);
        assertNotNull(inputStream);
        String url = "https://ap-test.unit4.com/oxalis/accessPointService";
        TransmissionRequest transmissionRequest = transmissionRequestBuilder
                .payLoad(inputStream)
                .overrideEndpointForStartProtocol(new URL(url))
                .build();
        assertEquals(transmissionRequest.getEndpointAddress().getUrl(), new URL(url));
        TransmissionResponse response = startMessageSender.send(transmissionRequest);
        assertNotNull(response);
    }

}

