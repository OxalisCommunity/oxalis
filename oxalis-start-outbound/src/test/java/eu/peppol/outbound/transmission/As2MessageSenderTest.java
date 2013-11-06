package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 29.10.13
 *         Time: 11:35
 */
@Test()
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
        String receiver = "9908:810017902";
        String sender = "9908:810017902";

        ParticipantId recipient = new ParticipantId(receiver);
        PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();
        SmpLookupManager.PeppolEndpointData endpointData = smpLookupManager.getEndpointData(recipient, documentTypeIdentifier);


        as2MessageSender.send(inputStream, recipient, new ParticipantId(sender), documentTypeIdentifier, endpointData.getUrl());
    }
}
