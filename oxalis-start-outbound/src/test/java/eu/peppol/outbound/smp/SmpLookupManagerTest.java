package eu.peppol.outbound.smp;

import eu.peppol.outbound.util.TestBase;
import eu.peppol.start.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.start.identifier.PeppolDocumentTypeId;
import eu.peppol.start.identifier.ParticipantId;
import eu.peppol.start.identifier.PeppolProcessTypeId;
import org.testng.annotations.Test;

import java.net.URL;
import java.security.cert.X509Certificate;

import static org.testng.Assert.*;

/**
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 9:05:52 AM
 */
@Test
public class SmpLookupManagerTest extends TestBase{

    private static PeppolDocumentTypeId invoice = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();
    //private static ParticipantId alfa1lab = Identifiers.getParticipantIdentifier("9902:DK28158815");
    private static ParticipantId alfa1lab = new ParticipantId("9902:DK28158815");
    private static ParticipantId helseVest = new ParticipantId("9908:983974724");
    private static ParticipantId sendRegning = new ParticipantId("9908:976098897");

    public void test01() throws Throwable {
        try {

            URL endpointAddress;
            endpointAddress = new SmpLookupManager().getEndpointAddress(sendRegning, invoice);
            assertEquals(endpointAddress.toExternalForm(), "https://aksesspunkt.sendregning.no/oxalis/accessPointService");

            endpointAddress = new SmpLookupManager().getEndpointAddress(alfa1lab, invoice);
            assertEquals(endpointAddress.toExternalForm(), "https://start-ap.alfa1lab.com:443/accessPointService");

            endpointAddress = new SmpLookupManager().getEndpointAddress(helseVest, invoice);
            assertEquals(endpointAddress.toExternalForm(), "https://peppolap.ibxplatform.net:8443/accesspointService");


        } catch (Throwable t) {
            signal(t);
        }
    }

    public void test02() throws Throwable {
        try {

            X509Certificate endpointCertificate;
            endpointCertificate = new SmpLookupManager().getEndpointCertificate(alfa1lab, invoice);
            assertEquals(endpointCertificate.getSerialNumber().toString(), "97394193891150626641360283873417712042");

//            endpointCertificate = new SmpLookupManager().getEndpointCertificate(helseVest, invoice);
//            assertEquals(endpointCertificate.getSerialNumber().toString(), "37276025795984990954710880598937203007");

        } catch (Throwable t) {
            signal(t);
        }
    }

    /**
     * Tests what happens when the participant is not registered.
     * @throws Throwable
     */
    public void test03() throws Throwable {

        URL endpointAddress;
        ParticipantId notRegisteredParticipant = new ParticipantId("1234:45678910");
        try {
            endpointAddress = new SmpLookupManager().getEndpointAddress(notRegisteredParticipant, invoice);
            fail(String.format("Participant '%s' should not be registered", notRegisteredParticipant));
        } catch (RuntimeException e) {
            //expected
        }
    }

    /**
     *
     */
    public void testGetFirstProcessIdentifier() throws SmpSignedServiceMetaDataException {
        PeppolProcessTypeId processTypeIdentifier = SmpLookupManager.getProcessIdentifierForDocumentType(new ParticipantId("9908:810017902"), PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());

        assertEquals(processTypeIdentifier.toString(), "urn:www.cenbii.eu:profile:bii04:ver1.0");

    }
}
