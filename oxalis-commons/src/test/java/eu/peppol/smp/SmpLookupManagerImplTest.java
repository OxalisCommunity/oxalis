package eu.peppol.smp;

import eu.peppol.identifier.*;
import eu.peppol.smp.*;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;
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
@Test(groups = {"integration"})
public class SmpLookupManagerImplTest {

    private static PeppolDocumentTypeId invoice = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();
    private static ParticipantId alfa1lab = new ParticipantId("9902:DK28158815");
    private static ParticipantId helseVest = new ParticipantId("9908:983974724");

    @Test
    public void test01() throws Throwable {

        URL endpointAddress;
        endpointAddress = new SmpLookupManagerImpl().getEndpointAddress(WellKnownParticipant.U4_TEST, invoice);
        assertEquals(endpointAddress.toExternalForm(), "https://aksesspunkt.sendregning.no/oxalis/accessPointService");

        endpointAddress = new SmpLookupManagerImpl().getEndpointAddress(alfa1lab, invoice);
        assertEquals(endpointAddress.toExternalForm(), "https://start-ap.alfa1lab.com:443/accessPointService");

        endpointAddress = new SmpLookupManagerImpl().getEndpointAddress(helseVest, invoice);
        assertEquals(endpointAddress.toExternalForm(), "https://peppolap.ibxplatform.net:8443/accessPointService");
    }

    @Test
    public void testSmpLookupProblem() {
        URL endpointAddress = new SmpLookupManagerImpl().getEndpointAddress(new ParticipantId("9908:971032081"), PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0#urn:www.difi.no:ehf:faktura:ver1::2.0"));
        assertNotNull(endpointAddress);
    }

    @Test
    public void test02() throws Throwable {

        X509Certificate endpointCertificate;
        endpointCertificate = new SmpLookupManagerImpl().getEndpointCertificate(alfa1lab, invoice);
        assertEquals(endpointCertificate.getSerialNumber().toString(), "56025519523792163866580293261663838570");
    }

    /**
     * Tests what happens when the participant is not registered.
     *
     * @throws Throwable
     */
    @Test
    public void test03() throws Throwable {

        ParticipantId notRegisteredParticipant = new ParticipantId("1234:45678910");
        try {
            new SmpLookupManagerImpl().getEndpointAddress(notRegisteredParticipant, invoice);
            fail(String.format("Participant '%s' should not be registered", notRegisteredParticipant));
        } catch (RuntimeException e) {
            //expected
        }
    }

    @Test
    public void testGetFirstProcessIdentifier() throws SmpSignedServiceMetaDataException {
        PeppolProcessTypeId processTypeIdentifier = new SmpLookupManagerImpl().getProcessIdentifierForDocumentType(WellKnownParticipant.U4_TEST, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());

        assertEquals(processTypeIdentifier.toString(), "urn:www.cenbii.eu:profile:bii04:ver1.0");

    }

    @Test
    public void testGetServiceGroup() throws SmpLookupException, ParticipantNotRegisteredException {

        List<PeppolDocumentTypeId> documentTypeIdList = new SmpLookupManagerImpl().getServiceGroups(WellKnownParticipant.U4_TEST);
        assertTrue(!documentTypeIdList.isEmpty());

        PeppolDocumentTypeId documentTypeId = documentTypeIdList.get(0);
        assertNotNull(documentTypeId.getLocalName(), "Invalid local name in document type");
        assertNotNull(documentTypeId.getRootNameSpace(), "Invalid root name space");
        assertNotNull(documentTypeId.getCustomizationIdentifier(), "Invalid customization identifier");
    }

    @Test
    public void testGetServiceGroupForNotRegisteredParticipant() throws SmpLookupException {

        ParticipantId ppid = new ParticipantId("SENDREGNING_TEST_PPID_OLD");

        try {
            List<PeppolDocumentTypeId> documentTypeIdList = new SmpLookupManagerImpl().getServiceGroups(ppid);

            fail("Execption should have been thrown");
        } catch (ParticipantNotRegisteredException e) {
            assertEquals(ppid, e.getParticipantId());
        }
    }

    @Test
    public void testGetEndpointData() {

        ParticipantId participantId = WellKnownParticipant.U4_TEST;

        SmpLookupManager.PeppolEndpointData peppolEndpointData = new SmpLookupManagerImpl().getEndpointData(participantId, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        assertNotNull(peppolEndpointData);
        assertNotNull(peppolEndpointData.getCommonName(), "CN attribute of certificate not provided");
    }

    @Test()
    public void testSmlHostnameOverride() {

        GlobalConfiguration configuration = GlobalConfiguration.getInstance();
        String overrideSml = "sml.difi.no";
        try {
            assertEquals(configuration.getSmlHostname(), "");
            assertNull(SmpLookupManagerImpl.checkForSmlHostnameOverride(null));
            assertEquals(SmpLookupManagerImpl.discoverSmlHost(), configuration.getModeOfOperation() == OperationalMode.TEST ? SmlHost.TEST_SML : SmlHost.PRODUCTION_SML);

            configuration.setSmlHostname(overrideSml);

            assertEquals(configuration.getSmlHostname(), overrideSml);
            assertEquals(SmpLookupManagerImpl.checkForSmlHostnameOverride(null).toString(), overrideSml);
            assertEquals(SmpLookupManagerImpl.discoverSmlHost().toString(), overrideSml);
        } finally {
            configuration.setSmlHostname("");
            assertEquals(configuration.getSmlHostname(), "");
        }
    }
}
