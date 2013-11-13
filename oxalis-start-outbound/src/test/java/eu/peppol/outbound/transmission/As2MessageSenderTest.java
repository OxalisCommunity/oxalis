package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.xml.ws.api.server.EndpointData;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 29.10.13
 *         Time: 11:35
 */
@Test(groups = {"integration"})
@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class})
public class As2MessageSenderTest {

    @Inject @Named("sampleXml")InputStream inputStream;

    @Inject SmpLookupManager smpLookupManager;

    /** Verifies that the Google Guice injection of @Named injections works as expected */
    @Test
    public void testInjection() throws Exception {
        assertNotNull(inputStream);
    }

    /** Creates a message sender and attempts to send the message */
    @Test(groups = {"integration"})
    public void sendSampleMessageAndVerify() throws Exception {

        As2MessageSender as2MessageSender = new As2MessageSender(smpLookupManager);
        String receiver = "OpenAS2A";
        String sender = "OpenAS2B";

        ParticipantId recipient = new ParticipantId(receiver);
        PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();
        SmpLookupManager.PeppolEndpointData endpointData = smpLookupManager.getEndpointData(recipient, documentTypeIdentifier);


        as2MessageSender.send(inputStream, recipient, new ParticipantId(sender), documentTypeIdentifier, endpointData.getUrl());
    }


    public void sendToItsligo() throws MalformedURLException {
        As2MessageSender as2MessageSender = new As2MessageSender(smpLookupManager);
        String receiver = "0088:itsligotest2";
        String sender = "9908:810017902";

        ParticipantId recipient = new ParticipantId(receiver);
        PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();

        SmpLookupManager.PeppolEndpointData endpointData = new SmpLookupManager.PeppolEndpointData(new URL("https://itsligoas2.eu/api/as2"), BusDoxProtocol.AS2);
        as2MessageSender.send(inputStream, recipient, new ParticipantId(sender), documentTypeIdentifier, endpointData.getUrl());
    }

    //@Test(enabled = false)
    public void sendToOpenAS2() throws MalformedURLException {
        As2MessageSender as2MessageSender = new As2MessageSender(smpLookupManager);
        String receiver = "9908:810017902";
        String sender = "9908:810017902";

        ParticipantId recipient = new ParticipantId(receiver);
        PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();

        SmpLookupManager.PeppolEndpointData endpointData = new SmpLookupManager.PeppolEndpointData(new URL("http://localhost:10080/HttpReceiver"), BusDoxProtocol.AS2);
        as2MessageSender.send(inputStream, recipient, new ParticipantId(sender), documentTypeIdentifier, endpointData.getUrl());
    }



}
