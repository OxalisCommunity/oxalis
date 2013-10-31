package eu.peppol.outbound.smp;

import eu.peppol.outbound.util.TestBase;
import eu.peppol.smp.*;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolProcessTypeId;
import org.testng.annotations.Test;

import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.testng.Assert.*;

/**
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 9:05:52 AM
 */
@Test(groups = "integration")
public class SmpLookupManagerImplTest extends TestBase {

    private static PeppolDocumentTypeId invoice = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();
    //private static ParticipantId alfa1lab = Identifiers.getParticipantIdentifier("9902:DK28158815");
    private static ParticipantId alfa1lab = new ParticipantId("9902:DK28158815");
    private static ParticipantId helseVest = new ParticipantId("9908:983974724");
    public static final String SR_TEST_PPID = "9908:810017902";
    private static ParticipantId sendRegning = new ParticipantId(SR_TEST_PPID);
    public static final ParticipantId SENDREGNING_TEST_PPID = new ParticipantId(SR_TEST_PPID);
    public static final ParticipantId SENDREGNING_TEST_PPID_OLD = new ParticipantId("9908:976098897");

    public void test01() throws Throwable {
        try {

            URL endpointAddress;
            endpointAddress = new SmpLookupManagerImpl().getEndpointAddress(sendRegning, invoice);
            assertEquals(endpointAddress.toExternalForm(), "https://aksesspunkt.sendregning.no/oxalis/accessPointService");

            endpointAddress = new SmpLookupManagerImpl().getEndpointAddress(alfa1lab, invoice);
            assertEquals(endpointAddress.toExternalForm(), "https://start-ap.alfa1lab.com:443/accessPointService");

            endpointAddress = new SmpLookupManagerImpl().getEndpointAddress(helseVest, invoice);
            assertEquals(endpointAddress.toExternalForm(), "https://peppolap.ibxplatform.net:8443/accessPointService");


        } catch (Throwable t) {
            signal(t);
        }
    }

    public void testSmpLookupProblem() {
        URL endpointAddress = new SmpLookupManagerImpl().getEndpointAddress(new ParticipantId("9908:971032081"), PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0#urn:www.difi.no:ehf:faktura:ver1::2.0"));
        assertNotNull(endpointAddress);
    }

    public void test02() throws Throwable {
        try {

            X509Certificate endpointCertificate;
            endpointCertificate = new SmpLookupManagerImpl().getEndpointCertificate(alfa1lab, invoice);
            assertEquals(endpointCertificate.getSerialNumber().toString(), "26596158403896804150415214044400823812");

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

        ParticipantId notRegisteredParticipant = new ParticipantId("1234:45678910");
        try {
            new SmpLookupManagerImpl().getEndpointAddress(notRegisteredParticipant, invoice);
            fail(String.format("Participant '%s' should not be registered", notRegisteredParticipant));
        } catch (RuntimeException e) {
            //expected
        }
    }

    /**
     *
     */
    public void testGetFirstProcessIdentifier() throws SmpSignedServiceMetaDataException {
        PeppolProcessTypeId processTypeIdentifier = new SmpLookupManagerImpl().getProcessIdentifierForDocumentType(new ParticipantId(SR_TEST_PPID), PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());

        assertEquals(processTypeIdentifier.toString(), "urn:www.cenbii.eu:profile:bii04:ver1.0");

    }

    public void testGetServiceGroup() throws SmpLookupException, ParticipantNotRegisteredException {

        List<PeppolDocumentTypeId> documentTypeIdList = new SmpLookupManagerImpl().getServiceGroups(SENDREGNING_TEST_PPID);
        assertTrue(!documentTypeIdList.isEmpty());

        PeppolDocumentTypeId documentTypeId = documentTypeIdList.get(0);
        assertNotNull(documentTypeId.getLocalName(),"Invalid local name in document type");
        assertNotNull(documentTypeId.getRootNameSpace(),"Invalid root name space");
        assertNotNull(documentTypeId.getCustomizationIdentifier(), "Invalid customization identifier");
        assertNotNull(documentTypeId.getCustomizationIdentifier().getTransactionIdentifier());

    }

    public void testGetServiceGroupForNotRegisteredParticipant() throws SmpLookupException{

        ParticipantId ppid = new ParticipantId("SENDREGNING_TEST_PPID_OLD");

        try{
            List<PeppolDocumentTypeId> documentTypeIdList = new SmpLookupManagerImpl().getServiceGroups(ppid);

            fail("Execption should have been thrown");
        } catch (ParticipantNotRegisteredException e) {
            assertEquals(ppid, e.getParticipantId());
        }
    }

    @Test
    public void testGetEndpointData() {

        ParticipantId participantId = new ParticipantId("9908:810017902");

        SmpLookupManager.PeppolEndpointData peppolEndpointData = new SmpLookupManagerImpl().getEndpointData(participantId, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        assertNotNull(peppolEndpointData);
    }

}
