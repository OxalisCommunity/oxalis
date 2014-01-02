package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.as2.InvalidAs2SystemIdentifierException;
import eu.peppol.as2.PeppolAs2SystemIdentifier;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.smp.SmpLookupManager;
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
@Guice(modules = {TransmissionTestITModule.class})
public class As2MessageSenderTestIT {

    @Inject @Named("sampleXml")InputStream inputStream;

    @Inject SmpLookupManager smpLookupManager;

    /** Verifies that the Google Guice injection of @Named injections works as expected */
    @Test
    public void testInjection() throws Exception {
        assertNotNull(inputStream);
    }

    /**
     * Requires our AS2 server to be up and running at https://localhost:8080/oxalis/as2
     *
     * @throws Exception
     */
    @Test(groups = {"integration"})
    public void sendSampleMessageAndVerify() throws Exception {

        As2MessageSender as2MessageSender = new As2MessageSender(smpLookupManager);
        String receiver = "9908:810017902";
        String sender = "9908:810017902";

        ParticipantId recipient = new ParticipantId(receiver);
        PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();
        SmpLookupManager.PeppolEndpointData endpointData = smpLookupManager.getEndpointTransmissionData(recipient, documentTypeIdentifier);
        assertNotNull(endpointData.getCommonName());

        as2MessageSender.send(inputStream, recipient, new ParticipantId(sender), documentTypeIdentifier, endpointData, PeppolAs2SystemIdentifier.valueOf(KeystoreManager.getInstance().getOurCommonName()));
    }


    /**
     * Sends a message to the Irish ITSligo AS2 server.
     *
     * Contact person is Edmund Gray
     *
     * @throws MalformedURLException
     */
    @Test(groups = {"manual"})
    public void sendToItsligo() throws MalformedURLException, InvalidAs2SystemIdentifierException {
        As2MessageSender as2MessageSender = new As2MessageSender(smpLookupManager);
        String receiver = "0088:itsligotest2";
        String sender = "9908:810017902";

        ParticipantId recipient = new ParticipantId(receiver);
        PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();

        SmpLookupManager.PeppolEndpointData endpointData = new SmpLookupManager.PeppolEndpointData(new URL("https://itsligoas2.eu/api/as2"), BusDoxProtocol.AS2,new CommonName("APP_1000000009"));
        as2MessageSender.send(inputStream, recipient, new ParticipantId(sender), documentTypeIdentifier, endpointData, PeppolAs2SystemIdentifier.valueOf(KeystoreManager.getInstance().getOurCommonName()));
    }


    /** Creates a message sender and attempts to send a message to the OpenAS2 server.
     *
     * This test is disabled as it is meant to be run manually
     * whenever the need is there, as it requires you to start the OpenAS2 server first.
     */
    @Test(groups = {"manual"})
    public void sendToOpenAS2() throws MalformedURLException, InvalidAs2SystemIdentifierException {
        As2MessageSender as2MessageSender = new As2MessageSender(smpLookupManager);
        String receiver = "9908:810017902";
        String sender = "9908:810017902";

        ParticipantId recipient = new ParticipantId(receiver);
        PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();

        SmpLookupManager.PeppolEndpointData endpointData = new SmpLookupManager.PeppolEndpointData(new URL("http://localhost:10080/HttpReceiver"), BusDoxProtocol.AS2, new CommonName("OpenAS2A"));

        // Must change the senders system identity in order to be accepted by OpenAS2
        as2MessageSender.send(inputStream, recipient, new ParticipantId(sender), documentTypeIdentifier, endpointData, new PeppolAs2SystemIdentifier("OpenAS2B"));
    }
}
