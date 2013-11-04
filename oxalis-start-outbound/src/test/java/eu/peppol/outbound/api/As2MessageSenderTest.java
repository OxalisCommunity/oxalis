package eu.peppol.outbound.api;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.outbound.guice.SmpTestModule;
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
@Guice(modules = {SmpTestModule.class, TestResourceModule.class})
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

        as2MessageSender.send(inputStream, receiver, sender, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());

    }
}
