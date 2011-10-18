package eu.peppol.inbound.smp;

import eu.peppol.inbound.sml.SmpLookup;
import eu.peppol.inbound.util.TestBase;
import eu.peppol.outbound.util.Constants;
import org.testng.annotations.Test;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;

import java.security.cert.X509Certificate;

import static org.testng.Assert.*;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 12:33:13 PM
 */
@Test
public class SmpTest extends TestBase {

    private static DocumentIdentifierType documentId = Constants.getInvoiceDocumentIdentifier();
    private static ProcessIdentifierType processId = Constants.getInvoiceProcessIdentifier();
    private static ParticipantIdentifierType recipient = Constants.getParticipantIdentifier("9902:DK28158815");

    public void test01() throws Throwable {
        try {

            String endpointAddress = SmpLookup.getEndpointAddress(recipient, documentId);
            assertEquals(endpointAddress, "https://start-ap.alfa1lab.com:443/accesspointService");

        } catch (Throwable t) {
            signal(t);
        }
    }

    public void test02() throws Throwable {
        try {

            X509Certificate endpointCertificate1 = SmpLookup.getEndpointCertificate(recipient, documentId, processId);
            assertEquals(endpointCertificate1.getSerialNumber().toString(), "97394193891150626641360283873417712042");

        } catch (Throwable t) {
            signal(t);
        }
    }
}
