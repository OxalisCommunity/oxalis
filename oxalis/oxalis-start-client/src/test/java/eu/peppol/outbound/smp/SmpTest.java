package eu.peppol.outbound.smp;

import eu.peppol.outbound.util.Identifiers;
import eu.peppol.outbound.util.TestBase;
import org.testng.annotations.Test;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;

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
    private static ProcessIdentifierType process = Identifiers.getInvoiceProcessIdentifier();
    private static ParticipantIdentifierType participant = Identifiers.getParticipantIdentifier("9902:DK28158815");

    public void test01() throws Throwable {
        try {

            URL endpointAddress = new SmpLookupManager().getEndpointAddress(participant, invoice);
            assertEquals(endpointAddress.toExternalForm(), "https://start-ap.alfa1lab.com:443/accesspointService");

        } catch (Throwable t) {
            signal(t);
        }
    }

    public void test02() throws Throwable {
        try {

            X509Certificate endpointCertificate = new SmpLookupManager().getEndpointCertificate(participant, invoice, process);
            assertEquals(endpointCertificate.getSerialNumber().toString(), "97394193891150626641360283873417712042");

        } catch (Throwable t) {
            signal(t);
        }
    }
}
