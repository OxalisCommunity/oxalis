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
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 9:05:52 AM
 */
@Test(groups = {"integration"})
public class SmpLookupManagerImplTest {

    private static PeppolDocumentTypeId invoice = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();
    private static ParticipantId alfa1lab = new ParticipantId("9902:DK28158815");
    private static ParticipantId helseVest = new ParticipantId("9908:983974724");
    private SmpLookupManagerImpl smpLookupManager;

    @BeforeMethod
    public void setUp() {
        smpLookupManager = new SmpLookupManagerImpl(new SmpContentRetrieverImpl(), new DefaultBusDoxProtocolSelectionStrategyImpl());
    }

    @Test
    public void test01() throws Throwable {

        URL endpointAddress;
        endpointAddress = smpLookupManager.getEndpointAddress(WellKnownParticipant.U4_TEST, invoice);
        assertEquals(endpointAddress.toExternalForm(), "https://aksesspunkt.sendregning.no/oxalis/accessPointService");

        endpointAddress = smpLookupManager.getEndpointAddress(alfa1lab, invoice);
        assertEquals(endpointAddress.toExternalForm(), "https://start-ap.alfa1lab.com:443/accessPointService");

        endpointAddress = smpLookupManager.getEndpointAddress(helseVest, invoice);
        assertEquals(endpointAddress.toExternalForm(), "https://peppolap.ibxplatform.net/accessPointService");
    }

    @Test
    public void testSmpLookupProblem() {
        URL endpointAddress = smpLookupManager.getEndpointAddress(new ParticipantId("9908:971032081"), PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0#urn:www.difi.no:ehf:faktura:ver1::2.0"));
        assertNotNull(endpointAddress);
    }

    @Test
    public void test02() throws Throwable {
        X509Certificate endpointCertificate;
        endpointCertificate = smpLookupManager.getEndpointCertificate(alfa1lab, invoice);
        assertEquals(endpointCertificate.getSerialNumber().toString(), "56025519523792163866580293261663838570");
    }

    /**
     * Tests what happens when the participant is not registered
     */
    @Test
    public void test03() throws Throwable {
        ParticipantId notRegisteredParticipant = new ParticipantId("1234:45678910"); // illegal prefix
        try {
            smpLookupManager.getEndpointAddress(notRegisteredParticipant, invoice);
            fail(String.format("Participant '%s' should not be registered", notRegisteredParticipant));
        } catch (RuntimeException e) {
            //expected
        }
    }

    /**
     * Tests what happens when the participant has been registered
     */
    @Test(expectedExceptions = { ParticipantNotRegisteredException.class } )
    public void test04() throws Exception {
        smpLookupManager.getServiceGroups(new ParticipantId("9908:976098897")); // not registered in ELMA as of 2014-06-12 (SendRegning)
        fail("This should throw ParticipantNotRegisteredException");
    }

    @Test(expectedExceptions = { ParticipantNotRegisteredException.class } )
    public void test05() throws Exception {
        smpLookupManager.getServiceGroups(new ParticipantId("0088:0935300003680")); // not registered in GLN as of 2014-06-12 (Illegal number)
        fail("This should throw ParticipantNotRegisteredException");
    }

    @Test
    public void testGetFirstProcessIdentifier() throws SmpSignedServiceMetaDataException {
        PeppolProcessTypeId processTypeIdentifier = smpLookupManager.getProcessIdentifierForDocumentType(WellKnownParticipant.U4_TEST, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        assertEquals(processTypeIdentifier.toString(), "urn:www.cenbii.eu:profile:bii04:ver1.0");
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

    @Test
    public void testGetEndpointData() {
        ParticipantId participantId = WellKnownParticipant.U4_TEST;
        SmpLookupManager.PeppolEndpointData peppolEndpointData = smpLookupManager.getEndpointTransmissionData(participantId, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
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

    @Test()
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

}
