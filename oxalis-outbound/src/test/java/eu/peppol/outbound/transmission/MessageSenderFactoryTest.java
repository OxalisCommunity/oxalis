package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 29.10.13
 *         Time: 18:20
 */

@Guice(modules = {TransmissionTestModule.class,TestResourceModule.class})
public class MessageSenderFactoryTest {

    @Inject
    SmpLookupManager smpLookupManager;

    @Inject @Named("sample-xml-with-sbdh")
    InputStream sampleMessageInputStream;

    /**
     * Verifies that the internal method for obtaining information on the destination access point, works
     * as expected, i.e. should return AS2 for PPID U4_TEST due to the fact that the mock SmpLookupManager will
     * always return "AS2" for U4_TEST
     *
     * @throws Exception
     */
    @Test
    public void testProtocolObtained() throws Exception {
        SmpLookupManager.PeppolEndpointData endpointData = smpLookupManager.getEndpointTransmissionData(WellKnownParticipant.U4_TEST, PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        assertNotNull(endpointData, "No endpoint data received");
        assertEquals(endpointData.getBusDoxProtocol(), BusDoxProtocol.AS2);
    }

}
