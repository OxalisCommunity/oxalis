package eu.peppol.outbound.api;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.outbound.guice.SmpTestModule;
import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 29.10.13
 *         Time: 18:20
 */

@Guice(modules = {SmpTestModule.class, TestResourceModule.class})
public class MessageSenderFactoryTest {

    @Inject
    MessageSenderFactory messageSenderFactory;

    @Inject @Named("sampleXml")
    InputStream sampleMessageInputStream;

    /**
     * Verifies that the internal method for obtaining information on the destination access point, works
     * as expected.
     * @throws Exception
     */
    @Test
    public void testProtocolObtained() throws Exception {

        SmpLookupManager.PeppolEndpointData endpointData = messageSenderFactory.getBusDoxProtocolFor(new ParticipantId("9908:810017902"), PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        assertEquals(endpointData.getBusDoxProtocol(), BusDoxProtocol.AS2);
    }

    @Test
    public void testSendAMessage() throws Exception {

        OutboundMessage outboundMessage = new OutboundMessage.Builder()
                .requiredContentsFrom(sampleMessageInputStream)
                .requiredDocumentType(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier())
                .requiredSender(new ParticipantId("9908:810017902"))
                .requiredReceiver(new ParticipantId("9908:810017902"))
                .build();

        MessageSender messageSender = messageSenderFactory.createMessageSender(outboundMessage.getReceiver(), outboundMessage.getPeppolDocumentTypeId());
        assertNotNull(messageSender);
    }
}
