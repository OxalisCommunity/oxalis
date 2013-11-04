package eu.peppol.outbound.api;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.outbound.guice.SmpTestModule;
import eu.peppol.outbound.guice.TestResourceModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 31.10.13
 *         Time: 14:17
 */
@Guice(modules = {SmpTestModule.class, TestResourceModule.class})
public class OutboundMessageTest {

    @Inject
    @Named("sampleXml")
    InputStream inputStream;

    @Test
    public void testBuilder() throws Exception {
        OutboundMessage outboundMessage = new OutboundMessage.Builder()
                .requiredReceiver(new ParticipantId("9908:810017902"))
                .requiredSender(new ParticipantId("9908:976098897"))
                .requiredDocumentType(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier())
                .requiredContentsFrom(inputStream)
                .build();

        assertNotNull(outboundMessage);
    }
}
