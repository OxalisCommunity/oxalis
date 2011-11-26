package eu.peppol.outbound.smp;

import eu.peppol.outbound.util.Identifiers;
import eu.peppol.outbound.util.TestBase;
import org.testng.annotations.Test;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;

import java.net.URL;
import java.security.cert.X509Certificate;

import static org.testng.Assert.*;

/**
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 9:05:52 AM
 */
@Test
public class SmpTest extends TestBase{

    private static DocumentIdentifierType invoice = Identifiers.getInvoiceDocumentIdentifier();
    private static ParticipantIdentifierType alfa1lab = Identifiers.getParticipantIdentifier("9902:DK28158815");
    private static ParticipantIdentifierType helseVest = Identifiers.getParticipantIdentifier("9908:983974724");

    public void test01() throws Throwable {
        try {

            URL endpointAddress;
            endpointAddress = new SmpLookupManager().getEndpointAddress(alfa1lab, invoice);
            assertEquals(endpointAddress.toExternalForm(), "https://start-ap.alfa1lab.com:443/accesspointService");
// Cap Gemini is down ...
//            endpointAddress = new SmpLookupManager().getEndpointAddress(helseVest, invoice);
//            assertEquals(endpointAddress.toExternalForm(), "https://peppolap.ibxplatform.net:8443/accesspointService");

        } catch (Throwable t) {
            signal(t);
        }
    }

    public void test02() throws Throwable {
        try {

            X509Certificate endpointCertificate;
            endpointCertificate = new SmpLookupManager().getEndpointCertificate(alfa1lab, invoice);
            assertEquals(endpointCertificate.getSerialNumber().toString(), "97394193891150626641360283873417712042");

// Cap Gemini is down ...
//            endpointCertificate = new SmpLookupManager().getEndpointCertificate(helseVest, invoice);
//            assertEquals(endpointCertificate.getSerialNumber().toString(), "37276025795984990954710880598937203007");

        } catch (Throwable t) {
            signal(t);
        }
    }
}
