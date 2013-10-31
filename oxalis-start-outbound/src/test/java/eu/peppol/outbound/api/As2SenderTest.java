package eu.peppol.outbound.api;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import eu.peppol.as2.As2DateUtil;
import eu.peppol.as2.As2DispositionNotificationOptions;
import eu.peppol.as2.As2Header;
import eu.peppol.as2.As2Message;
import eu.peppol.outbound.guice.SmpTestModule;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 29.10.13
 *         Time: 11:35
 */
@Test()
@Guice(modules = {SmpTestModule.class})
public class As2SenderTest {

    @Inject @Named("sampleXml")InputStream inputStream;

    @Inject SmpLookupManager smpLookupManager;

    @Test
    public void testInjection() throws Exception {
        assertNotNull(inputStream);

    }

    @Test
    public void sendSampleMessageAndVerify() throws Exception {

        As2Sender as2Sender = new As2Sender(smpLookupManager);
        String receiver = "9908:810017902";
        String sender = "9908:810017902";

        as2Sender.send(inputStream, receiver, sender, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());

    }
}
