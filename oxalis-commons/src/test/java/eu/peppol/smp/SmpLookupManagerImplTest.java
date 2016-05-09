/*
 * Copyright (c) 2011,2012,2013,2014 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.smp;

import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.*;
import eu.peppol.security.CommonName;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;
import org.busdox.smp.EndpointType;
import org.busdox.smp.SignedServiceMetadataType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author nigel
 * @author thore
 */
@Test(groups = {"integration"})
public class SmpLookupManagerImplTest {

    private static PeppolDocumentTypeId ehfInvoice = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();
    private static PeppolDocumentTypeId bisInvoice = PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0::2.1");
    private static PeppolDocumentTypeId bisOrderResponse = PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:www.cenbii.eu:transaction:biitrns076:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver2.0::2.1");

    private static ParticipantId unit4SwedenPart = new ParticipantId("0088:7368846885001");
    private static ParticipantId helseVest = new ParticipantId("9908:983974724");
    private static ParticipantId sendRegning = new ParticipantId("9908:810017902");
    private static ParticipantId foreignPart = new ParticipantId("0088:5798009883964");
    private static ParticipantId foreignFormatTestPart = new ParticipantId("0088:5798009883964");

    private SmpLookupManagerImpl smpLookupManager;

    @BeforeMethod
    public void setUp() {
        smpLookupManager = new SmpLookupManagerImpl(new SmpContentRetrieverImpl(), new DefaultBusDoxProtocolSelectionStrategyImpl());
    }

    @Test
    public void testSomeKnownEndpoints() throws Throwable {

        URL endpointAddress;
        endpointAddress = smpLookupManager.getEndpointAddress(WellKnownParticipant.U4_TEST, ehfInvoice);
        assertEquals(endpointAddress.toExternalForm(), "https://ap.unit4.com/oxalis/as2");

        endpointAddress = smpLookupManager.getEndpointAddress(unit4SwedenPart, bisInvoice);
        assertEquals(endpointAddress.toExternalForm(), "https://ap.unit4.com/oxalis/as2");

        endpointAddress = smpLookupManager.getEndpointAddress(helseVest, ehfInvoice);
        assertEquals(endpointAddress.toExternalForm(), "https://peppolap.ibxplatform.net/as2/as2");

    }

    @Test
    public void testGetServiceMetaData() throws Exception {

        String elma = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol4a:ver2.0:extended:urn:www.difi.no:ehf:faktura:ver2.0::2.1";
        SignedServiceMetadataType metaData = smpLookupManager.getServiceMetaData(sendRegning, PeppolDocumentTypeId.valueOf(elma));
        assertNotNull(metaData);
        assertNotNull(metaData.getServiceMetadata());
        assertNotNull(metaData.getServiceMetadata().getServiceInformation());
        assertNotNull(metaData.getServiceMetadata().getServiceInformation().getProcessList());
        assertFalse(metaData.getServiceMetadata().getServiceInformation().getProcessList().getProcess().isEmpty());
        assertEquals(metaData.getServiceMetadata().getServiceInformation().getProcessList().getProcess().get(0).getProcessIdentifier().getValue(), "urn:www.cenbii.eu:profile:bii04:ver2.0");

    }

    @Test
    public void testSmlLookupOfEhf20Invoice() throws Exception {

        String elma = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol4a:ver2.0:extended:urn:www.difi.no:ehf:faktura:ver2.0::2.1";
        URL endpointElma = smpLookupManager.getEndpointAddress(sendRegning, PeppolDocumentTypeId.valueOf(elma));
        assertNotNull(endpointElma);

    }

    @Test
    public void testSmlLookupOfEhf20CreditNote() throws Exception {

        // taken from ELMA lookup at : http://vefa.difi.no/smp/9908/810017902
        String elma = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.cenbii.eu:profile:biixx:ver2.0:extended:urn:www.difi.no:ehf:kreditnota:ver2.0::2.1";
        URL endpointElma = smpLookupManager.getEndpointAddress(sendRegning, PeppolDocumentTypeId.valueOf(elma));
        assertNotNull(endpointElma);

        // taken from VEFA validator and examples at https://github.com/difi/vefa-validator-conf/blob/master/STANDARD/EHFInvoice/2.0/test/BII05%20T14%20gyldig%20kreditnota.xml
        // String vefa = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0:extended:urn:www.difi.no:ehf:kreditnota:ver2.0::2.1";
        // URL endpointVefa = smpLookupManager.getEndpointAddress(sendRegning, PeppolDocumentTypeId.valueOf(vefa));
        // assertNotNull(endpointVefa);

    }

    @Test
    public void testSmpLookupProblem() {
        URL endpointAddress = smpLookupManager.getEndpointAddress(new ParticipantId("9908:971032081"), PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol4a:ver2.0:extended:urn:www.difi.no:ehf:faktura:ver2.0::2.1"));
        assertNotNull(endpointAddress);
    }

    @Test
    public void testSmpLookupOfForeignPartNotInELMA() throws Throwable {
        X509Certificate endpointCertificate;
        endpointCertificate = smpLookupManager.getEndpointCertificate(foreignPart, ehfInvoice);
        assertEquals(endpointCertificate.getSerialNumber().toString(), "160385440487707971146414839722670157110");
    }

    @Test
    public void testSmpLookupOfNonEHFFormatNotInELMA() throws Throwable {
        X509Certificate endpointCertificate;
        endpointCertificate = smpLookupManager.getEndpointCertificate(foreignFormatTestPart, bisOrderResponse);
        assertEquals(endpointCertificate.getSerialNumber().toString(), "160385440487707971146414839722670157110");
    }

    /**
     * Tests what happens when the participant is not registered
     */
    @Test
    public void test03() throws Throwable {
        ParticipantId notRegisteredParticipant = new ParticipantId("1234:45678910"); // illegal prefix
        try {
            smpLookupManager.getEndpointAddress(notRegisteredParticipant, ehfInvoice);
            fail(String.format("Participant '%s' should not be registered", notRegisteredParticipant));
        } catch (RuntimeException e) {
            //expected
        }
    }

    /**
     * Tests what happens when the participant has been registered
     */
    @Test(expectedExceptions = {ParticipantNotRegisteredException.class})
    public void test04() throws Exception {
        smpLookupManager.getServiceGroups(new ParticipantId("9908:976098897")); // not registered in ELMA as of 2014-06-12 (SendRegning)
        fail("This should throw ParticipantNotRegisteredException");
    }

    @Test(expectedExceptions = {ParticipantNotRegisteredException.class})
    public void test05() throws Exception {
        smpLookupManager.getServiceGroups(new ParticipantId("0088:0935300003680")); // not registered in GLN as of 2014-06-12 (Illegal number)
        fail("This should throw ParticipantNotRegisteredException");
    }

    @Test
    public void testGetFirstProcessIdentifier() throws SmpSignedServiceMetaDataException {
        PeppolProcessTypeId processTypeIdentifier = smpLookupManager.getProcessIdentifierForDocumentType(WellKnownParticipant.U4_TEST, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        assertEquals(processTypeIdentifier.toString(), "urn:www.cenbii.eu:profile:bii04:ver2.0");
    }

    @Test
    public void testGetServiceGroup() throws SmpLookupException, ParticipantNotRegisteredException {
        List<PeppolDocumentTypeId> documentTypeIdList = smpLookupManager.getServiceGroups(WellKnownParticipant.U4_TEST);
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
            List<PeppolDocumentTypeId> documentTypeIdList = smpLookupManager.getServiceGroups(ppid);
            // this is not supposed to happen, print all results we got then make the test fail
            for (PeppolDocumentTypeId d : documentTypeIdList) {
                System.out.println(d.toDebugString());
            }
            fail("Execption should have been thrown");
        } catch (ParticipantNotRegisteredException e) {
            assertEquals(ppid, e.getParticipantId());
        }
    }


    /**
     * This test was added 2015-11-03 to check this : https://github.com/difi/oxalis/issues/235
     */
    @Test
    public void testGetServiceGroupFromSmpUsingUtf8Bom() throws SmpLookupException, ParticipantNotRegisteredException {
        List<PeppolDocumentTypeId> documentTypeIdList = smpLookupManager.getServiceGroups(new ParticipantId("9908:994496093"));
        assertTrue(!documentTypeIdList.isEmpty());
    }

    @Test
    public void testGetEndpointData() {
        ParticipantId participantId = WellKnownParticipant.U4_TEST;
        SmpLookupManager.PeppolEndpointData peppolEndpointData = smpLookupManager.getEndpointTransmissionData(participantId, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        assertNotNull(peppolEndpointData);
        assertNotNull(peppolEndpointData.getCommonName(), "CN attribute of certificate not provided");
    }

    @Test
    public void testSmlHostnameOverride() {
        GlobalConfiguration configuration = GlobalConfiguration.getInstance();
        String backup = configuration.getSmlHostname();
        try {
            configuration.setSmlHostname("");
            // make sure we start without overridden default values
            assertEquals(SmpLookupManagerImpl.discoverSmlHost(), configuration.getModeOfOperation() == OperationalMode.TEST ? SmlHost.TEST_SML : SmlHost.PRODUCTION_SML);
            assertTrue(configuration.getSmlHostname().isEmpty());
            // make sure we can override
            String overrideSml = "sml.difi.no";
            configuration.setSmlHostname(overrideSml);
            assertEquals(configuration.getSmlHostname(), overrideSml);
            assertEquals(SmpLookupManagerImpl.checkForSmlHostnameOverride(null).toString(), overrideSml);
            assertEquals(SmpLookupManagerImpl.discoverSmlHost().toString(), overrideSml);
        } finally {
            configuration.setSmlHostname(backup);
        }
    }

    @Test
    public void parseSmpResponseWithTwoEntries() throws ParserConfigurationException, JAXBException, SAXException, IOException {

        final InputStream inputStream = SmpLookupManagerImplTest.class.getClassLoader().getResourceAsStream("smp-response-with-as2.xml");
        assertNotNull(inputStream, "Unable to find smp-response-with-as2.xml in the class path");

        // creates a mock content retriever
        SmpContentRetriever mockContentRetriever = new SmpContentRetriever() {
            @Override
            public InputSource getUrlContent(URL url) {
                return null;
            }
        };

        // Which is used by the concrete implementation of SmpLookupManager
        SmpLookupManagerImpl smpLookupManager = new SmpLookupManagerImpl(mockContentRetriever, new DefaultBusDoxProtocolSelectionStrategyImpl());

        // Provides a sample XML response from the SMP
        InputSource inputSource = new InputSource(inputStream);
        Document document = smpLookupManager.createXmlDocument(inputSource);

        // Parses the response into a typed object
        SignedServiceMetadataType signedServiceMetadataType = smpLookupManager.parseSmpResponseIntoSignedServiceMetadataType(document);
        assertNotNull(signedServiceMetadataType);

        // This is the actual test, where we try to get the endpoint profile
        EndpointType endpointType = smpLookupManager.selectOptimalEndpoint(signedServiceMetadataType);
        String transportProfile = endpointType.getTransportProfile();
        BusDoxProtocol busDoxProtocol = BusDoxProtocol.instanceFrom(transportProfile);

        assertEquals(busDoxProtocol, BusDoxProtocol.AS2, "Expected the AS2 protocol to be selected");

    }

    @Test
    public void parseSmpResponseWithUnknownEntry() throws ParserConfigurationException, JAXBException, SAXException, IOException {

        final InputStream inputStream = SmpLookupManagerImplTest.class.getClassLoader().getResourceAsStream("smp-response-with-unknown-protocol.xml");
        assertNotNull(inputStream, "Unable to find smp-response-with-unknown-protocol.xml in the class path");

        // creates a mock content retriever
        SmpContentRetriever mockContentRetriever = new SmpContentRetriever() {
            @Override
            public InputSource getUrlContent(URL url) {
                return null;
            }
        };

        // Which is used by the concrete implementation of SmpLookupManager
        SmpLookupManagerImpl smpLookupManager = new SmpLookupManagerImpl(mockContentRetriever, new DefaultBusDoxProtocolSelectionStrategyImpl());

        // Provides a sample XML response from the SMP
        InputSource inputSource = new InputSource(inputStream);
        Document document = smpLookupManager.createXmlDocument(inputSource);

        // Parses the response into a typed object
        SignedServiceMetadataType signedServiceMetadataType = smpLookupManager.parseSmpResponseIntoSignedServiceMetadataType(document);
        assertNotNull(signedServiceMetadataType);

        // This is the actual test, where we try to get the endpoint profile
        EndpointType endpointType = smpLookupManager.selectOptimalEndpoint(signedServiceMetadataType);
        String transportProfile = endpointType.getTransportProfile();
        BusDoxProtocol busDoxProtocol = BusDoxProtocol.instanceFrom(transportProfile);

        assertEquals(busDoxProtocol, BusDoxProtocol.AS2, "Expected code to skip the unknown protocol and return AS2 protocol");

    }

    @Test
    public void makeSureEndpointsAreEqual() throws Exception {
        SmpLookupManager.PeppolEndpointData e1 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2, new CommonName("cn"));
        SmpLookupManager.PeppolEndpointData e2 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2, new CommonName("cn"));
        assertTrue(e1.equals(e1));
        assertTrue(e2.equals(e2));
        assertEquals(e1, e2);
    }

    @Test
    public void makeSureEndpointsAreStillEqual() throws Exception {
        SmpLookupManager.PeppolEndpointData e1 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.START);
        SmpLookupManager.PeppolEndpointData e2 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.START, null);
        assertTrue(e1.equals(e1));
        assertTrue(e2.equals(e2));
        assertEquals(e1, e2);
    }

    @Test
    public void makeSureEndpointsDontMatchCN() throws Exception {
        SmpLookupManager.PeppolEndpointData e1 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2, new CommonName("cn"));
        SmpLookupManager.PeppolEndpointData e2 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2, new CommonName("not-equal"));
        assertNotEquals(e1, e2);
    }

    @Test
    public void makeSureEndpointsDontMatchProtocol() throws Exception {
        SmpLookupManager.PeppolEndpointData e1 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2, new CommonName("cn"));
        SmpLookupManager.PeppolEndpointData e2 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.START, new CommonName("not-equal"));
        assertNotEquals(e1, e2);
    }

    @Test
    public void makeSureEndpointsDontMatchUrl() throws Exception {
        SmpLookupManager.PeppolEndpointData e1 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2, new CommonName("cn"));
        SmpLookupManager.PeppolEndpointData e2 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as4"), BusDoxProtocol.AS2, new CommonName("cn"));
        assertNotEquals(e1, e2);
    }

}