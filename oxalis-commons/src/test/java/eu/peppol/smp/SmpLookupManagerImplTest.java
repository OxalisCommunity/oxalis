/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.smp;

import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.*;
import eu.peppol.security.CommonName;
import eu.peppol.util.OperationalMode;
import org.busdox.servicemetadata.publishing._1.EndpointType;
import org.busdox.servicemetadata.publishing._1.SignedServiceMetadataType;
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
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author nigel
 * @author steinar
 * @author thore
 */
@Test(groups = {"integration"})
public class SmpLookupManagerImplTest {

    private static PeppolDocumentTypeId ehfInvoice = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();
    private static PeppolDocumentTypeId bisInvoice = PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0::2.1");
    private static PeppolDocumentTypeId bisOrderResponse = PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2::OrderResponse##urn:www.cenbii.eu:transaction:biitrns076:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver2.0::2.1");

    private static ParticipantId difi_test = WellKnownParticipant.DIFI_TEST;

    private static ParticipantId foreignPart = new ParticipantId("0088:5798009883964");
    private static ParticipantId foreignFormatTestPart = new ParticipantId("0088:5798009883964");

    SmpLookupManagerImpl smpLookupManager;
    private SmpContentRetriever mockContentRetriever;
    private SmpContentRetrieverImpl smpContentRetriever;

    @BeforeMethod
    public void setUp() {
        // creates a mock content retriever
        mockContentRetriever = new SmpContentRetriever() {
            @Override
            public InputSource getUrlContent(URL url) {
                return null;
            }
        };

        smpContentRetriever = new SmpContentRetrieverImpl();

        smpLookupManager = new SmpLookupManagerImpl(smpContentRetriever, new DefaultBusDoxProtocolSelectionStrategyImpl(), OperationalMode.TEST, SmlHost.TEST_SML);
        assertNotNull(smpLookupManager, "Guice injection seemed to have failed");
    }

    @Test
    public void testSomeKnownEndpoints() throws Throwable {

        URL endpointAddress;
        endpointAddress = smpLookupManager.getEndpointAddress(WellKnownParticipant.DIFI_TEST, ehfInvoice);
        assertEquals(endpointAddress.toExternalForm(), "https://test-aksesspunkt.difi.no/as2");
    }

    @Test
    public void testGetServiceMetaData() throws Exception {

        String elma = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol4a:ver2.0:extended:urn:www.difi.no:ehf:faktura:ver2.0::2.1";
        SignedServiceMetadataType metaData = smpLookupManager.getServiceMetaData(WellKnownParticipant.DIFI_TEST, PeppolDocumentTypeId.valueOf(elma));
        assertNotNull(metaData);
        assertNotNull(metaData.getServiceMetadata());
        assertNotNull(metaData.getServiceMetadata().getServiceInformation());
        assertNotNull(metaData.getServiceMetadata().getServiceInformation().getProcessList());
        assertFalse(metaData.getServiceMetadata().getServiceInformation().getProcessList().getProcess().isEmpty());
        assertEquals(metaData.getServiceMetadata().getServiceInformation().getProcessList().getProcess().get(0).getProcessIdentifier().getValue(), "urn:www.cenbii.eu:profile:bii04:ver2.0");

    }

    @Test
    public void testSmlLookupOfEhf20Invoice() throws Exception {

        String docType = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol4a:ver2.0:extended:urn:www.difi.no:ehf:faktura:ver2.0::2.1";
        URL endpointElma = smpLookupManager.getEndpointAddress(difi_test, PeppolDocumentTypeId.valueOf(docType));
        assertNotNull(endpointElma);

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
        PeppolProcessTypeId processTypeIdentifier = smpLookupManager.getProcessIdentifierForDocumentType(WellKnownParticipant.DIFI_TEST, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        assertEquals(processTypeIdentifier.toString(), "urn:www.cenbii.eu:profile:bii04:ver2.0");
    }

    @Test
    public void testGetServiceGroup() throws SmpLookupException, ParticipantNotRegisteredException {
        List<PeppolDocumentTypeId> documentTypeIdList = smpLookupManager.getServiceGroups(WellKnownParticipant.DIFI_TEST);
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
    @Test(groups = {"manual"}, enabled = false)
    public void testGetServiceGroupFromSmpUsingUtf8Bom() throws SmpLookupException, ParticipantNotRegisteredException {
        List<PeppolDocumentTypeId> documentTypeIdList = smpLookupManager.getServiceGroups(new ParticipantId("9908:994496093"));
        assertTrue(!documentTypeIdList.isEmpty());
    }

    @Test
    public void testGetEndpointData() {
        ParticipantId participantId = WellKnownParticipant.DIFI_TEST;
        SmpLookupManager.PeppolEndpointData peppolEndpointData = smpLookupManager.getEndpointTransmissionData(participantId, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        assertNotNull(peppolEndpointData);
        assertNotNull(peppolEndpointData.getCommonName(), "CN attribute of certificate not provided");
    }

    @Test()
    public void testSmlHostnameOverride() {
        SmlHost overrideSml = new SmlHost("sml.difi.no");
        SmpLookupManagerImpl testSmpLookupManager = new SmpLookupManagerImpl(smpContentRetriever, new DefaultBusDoxProtocolSelectionStrategyImpl(), OperationalMode.TEST,overrideSml );
        // make sure we can override
        assertEquals(testSmpLookupManager.checkForSmlHostnameOverride(overrideSml), overrideSml, "Seems overriding SmlHost does not work.");
        assertEquals(testSmpLookupManager.discoverSmlHost(), overrideSml,"Seems we are reverting back to the original SmlHost");


    }


    @Test()
    public void parseSmpResponseWithTwoEntries() throws ParserConfigurationException, JAXBException, SAXException, IOException {

        final InputStream inputStream = SmpLookupManagerImplTest.class.getClassLoader().getResourceAsStream("smp-response-with-as2.xml");
        assertNotNull(inputStream, "Unable to find smp-response-with-as2.xml in the class path");


        // Which is used by the concrete implementation of SmpLookupManager
        SmpLookupManagerImpl smpLookupManager = new SmpLookupManagerImpl(mockContentRetriever, new DefaultBusDoxProtocolSelectionStrategyImpl(),OperationalMode.TEST, SmlHost.TEST_SML);

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
    public void makeSureEndpointsAreEqual() throws Exception {
        SmpLookupManager.PeppolEndpointData e1 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2, new CommonName("cn"));
        SmpLookupManager.PeppolEndpointData e2 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2, new CommonName("cn"));
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
    public void makeSureEndpointsDontMatchUrl() throws Exception {
        SmpLookupManager.PeppolEndpointData e1 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2, new CommonName("cn"));
        SmpLookupManager.PeppolEndpointData e2 = new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as4"), BusDoxProtocol.AS2, new CommonName("cn"));
        assertNotEquals(e1, e2);
    }

    /**
     * The SMP response for this PPID contains byte order mark, which causes the parsing of the response to fail.
     *
     * @throws SmpLookupException
     * @throws ParticipantNotRegisteredException
     */
    @Test(enabled = false)
    public void testGetServiceGroupProblematicOrgNumber() throws SmpLookupException, ParticipantNotRegisteredException {
        List<PeppolDocumentTypeId> documentTypeIdList = smpLookupManager.getServiceGroups(new ParticipantId("9908:994496093"));
        assertTrue(!documentTypeIdList.isEmpty());
    }

    @Test
    public void verifyHandlingOfSmlHostOverride() {
        SmpLookupManagerImpl smpLookupManager = new SmpLookupManagerImpl(new SmpContentRetrieverImpl(), new DefaultBusDoxProtocolSelectionStrategyImpl(), OperationalMode.TEST, null);
        SmlHost smlHost = smpLookupManager.discoverSmlHost();
        assertNotNull(smlHost);
        assertNotNull(smlHost.toString());
        assertFalse(smlHost.toString().trim().length() == 0);
        smlHost.equals(SmlHost.TEST_SML);
    }
}