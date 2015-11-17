package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.*;
import eu.peppol.outbound.OxalisOutboundModule;
import eu.peppol.outbound.guice.TestResourceModule;
import org.junit.Ignore;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * These tests needs TransmissionRequestBuilder to run in TEST-mode to be able to override values
 *
 * @author steinar
 * @author thore
 */
@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class })
public class TransmissionRequestBuilderIT {

    @Inject
    OverridableTransmissionRequestBuilderCreator overridableTransmissionRequestBuilderCreator;

    @Inject @Named("sample-xml-with-sbdh")
    InputStream inputStreamWithSBDH;

    @Inject @Named("sample-xml-no-sbdh")
    InputStream noSbdhInputStream;

    @Inject @Named("sample-xml-missing-metadata")
    InputStream missingMetadataInputStream;

    @Inject @Named("test-files-with-identification")
    public Map<String, PeppolStandardBusinessHeader> testFilesForIdentification;

    @Inject @Named("test-non-ubl-documents")
    public Map<String, PeppolStandardBusinessHeader> testNonUBLFiles;

    TransmissionRequestBuilder transmissionRequestBuilder;

    @BeforeMethod
    public void setUp() {
        transmissionRequestBuilder = overridableTransmissionRequestBuilderCreator.createTansmissionRequestBuilderAllowingOverrides();
        inputStreamWithSBDH.mark(Integer.MAX_VALUE);
        noSbdhInputStream.mark(Integer.MAX_VALUE);
    }

    @AfterMethod
    public void tearDown() throws IOException {
        inputStreamWithSBDH.reset();
        noSbdhInputStream.reset();
    }

    @Test
    public void makeSureWeAllowOverrides() {
        assertNotNull(transmissionRequestBuilder);
        assertTrue(transmissionRequestBuilder.isOverrideAllowed());
    }

    @Test
    public void createTransmissionRequestBuilderWithOnlyTheMessageDocument() throws Exception {

        assertNotNull(transmissionRequestBuilder);
        assertNotNull(inputStreamWithSBDH);

        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);

        // Builds the actual transmission request
        TransmissionRequest transmissionRequest = transmissionRequestBuilder.build();

        PeppolStandardBusinessHeader sbdh = transmissionRequestBuilder.getEffectiveStandardBusinessHeader();
        assertNotNull(sbdh);
        assertEquals(sbdh.getRecipientId(), WellKnownParticipant.U4_TEST);

        assertNotNull(transmissionRequest.getEndpointAddress());

        assertNotNull(transmissionRequest.getPeppolStandardBusinessHeader());

        assertEquals(transmissionRequest.getPeppolStandardBusinessHeader().getRecipientId(), WellKnownParticipant.U4_TEST);

        assertEquals(transmissionRequest.getEndpointAddress().getBusDoxProtocol(), BusDoxProtocol.AS2);

    }

    @Test
    @Ignore("This test connects to the internet but there is no internet access on DIGIT's continuous platform. The access should me mocked")
    public void xmlWithNoSBDH() throws Exception {

        TransmissionRequestBuilder builder = transmissionRequestBuilder.payLoad(noSbdhInputStream).receiver(WellKnownParticipant.DIFI);
        TransmissionRequest request = builder.build();

        assertNotNull(builder);
        assertNotNull(builder.getEffectiveStandardBusinessHeader(), "Effective SBDH is null");

        assertEquals(builder.getEffectiveStandardBusinessHeader().getRecipientId(), WellKnownParticipant.DIFI, "Receiver has not been overridden");
        assertEquals(request.getPeppolStandardBusinessHeader().getRecipientId(), WellKnownParticipant.DIFI);

    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void createTransmissionRequestWithStartAndSbdh() throws MalformedURLException {
        transmissionRequestBuilder.overrideEndpointForStartProtocol(new URL("http://localhost:8443/bla/bla"));
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.build();
    }

    @Test
    public void overrideFields() throws Exception {

        TransmissionRequestBuilder builder = transmissionRequestBuilder.payLoad(noSbdhInputStream)
                .sender(WellKnownParticipant.DIFI)
                .receiver(WellKnownParticipant.U4_TEST)
                .documentType(PeppolDocumentTypeIdAcronym.ORDER.getDocumentTypeIdentifier());

        TransmissionRequest request = builder.build();

        assertEquals(request.getEndpointAddress().getBusDoxProtocol(), BusDoxProtocol.AS2);
        assertEquals(request.getPeppolStandardBusinessHeader().getRecipientId(), WellKnownParticipant.U4_TEST);
        assertEquals(request.getPeppolStandardBusinessHeader().getSenderId(), WellKnownParticipant.DIFI);
        assertEquals(request.getPeppolStandardBusinessHeader().getDocumentTypeIdentifier(), PeppolDocumentTypeIdAcronym.ORDER.getDocumentTypeIdentifier());

    }

    @Test
    public void overrideMessageId() throws Exception {

        TransmissionRequestBuilder uniqueBuilder = transmissionRequestBuilder.payLoad(noSbdhInputStream)
                .sender(WellKnownParticipant.DIFI)
                .receiver(WellKnownParticipant.U4_TEST)
                .documentType(PeppolDocumentTypeIdAcronym.ORDER.getDocumentTypeIdentifier())
                .processType(PeppolProcessTypeIdAcronym.ORDER_ONLY.getPeppolProcessTypeId());

        TransmissionRequest requestWithUniqueMessageId = uniqueBuilder.build();
        MessageId originalMessageId = requestWithUniqueMessageId.getPeppolStandardBusinessHeader().getMessageId();

        // reset input stream so that we can re-read the exact same stream
        noSbdhInputStream.reset();

        TransmissionRequestBuilder identicalBuilder = transmissionRequestBuilder.payLoad(noSbdhInputStream)
                .sender(WellKnownParticipant.DIFI)
                .receiver(WellKnownParticipant.U4_TEST)
                .documentType(PeppolDocumentTypeIdAcronym.ORDER.getDocumentTypeIdentifier())
                .processType(PeppolProcessTypeIdAcronym.ORDER_ONLY.getPeppolProcessTypeId())
                .messageId(originalMessageId);
        TransmissionRequest requestWithIdenticalMessageId = identicalBuilder.build();
        MessageId identicalMessageId = requestWithIdenticalMessageId.getPeppolStandardBusinessHeader().getMessageId();

        // make sure the overridden messageId matches the one we provided
        assertNotNull(identicalMessageId);
        assertNotNull(originalMessageId);
        assertEquals(identicalMessageId, originalMessageId);

    }

    @Test
    public void testOverrideEndPoint() throws Exception {
        assertNotNull(inputStreamWithSBDH);
        URL url = new URL("http://localhost:8080/oxalis/as2");
        TransmissionRequest request = transmissionRequestBuilder
                .payLoad(inputStreamWithSBDH)
                .overrideAs2Endpoint(url, "APP_1000000006").build();
        assertEquals(request.getEndpointAddress().getBusDoxProtocol(), BusDoxProtocol.AS2);
        assertEquals(request.getEndpointAddress().getUrl(), url);
    }

    @Test
    public void testOverrideOfAllValues() throws Exception {
        MessageId messageId = new MessageId("messageid");
        TransmissionRequest request = transmissionRequestBuilder
                .payLoad(inputStreamWithSBDH)
                .sender(WellKnownParticipant.DIFI)
                .receiver(WellKnownParticipant.U4_TEST)
                .documentType(PeppolDocumentTypeIdAcronym.ORDER.getDocumentTypeIdentifier())
                .processType(PeppolProcessTypeIdAcronym.ORDER_ONLY.getPeppolProcessTypeId())
                .messageId(messageId)
                .build();
        PeppolStandardBusinessHeader meta = request.getPeppolStandardBusinessHeader();
        assertEquals(meta.getSenderId(), WellKnownParticipant.DIFI);
        assertEquals(meta.getRecipientId(), WellKnownParticipant.U4_TEST);
        assertEquals(meta.getDocumentTypeIdentifier(), PeppolDocumentTypeIdAcronym.ORDER.getDocumentTypeIdentifier());
        assertEquals(meta.getProfileTypeIdentifier(), PeppolProcessTypeIdAcronym.ORDER_ONLY.getPeppolProcessTypeId());
        assertEquals(meta.getMessageId(), messageId);
    }

    @Test
    public void makeSureWeDetectMissingProperties() {
        try {
            transmissionRequestBuilder
                    .payLoad(missingMetadataInputStream)
                    .build();
            fail("The build() should have failed indicating missing properties");
        } catch (Exception ex) {
            assertEquals(ex.getMessage(), "TransmissionRequest can not be built, missing [recipientId, senderId] metadata.");
        }
    }

    /**
     * Test decoding of various PEPPOL UBL / EHF document types.
     * Make sure type, profile, customization, version, sender and receiver are retrieved correctly from all.
     */
    @Test
    public void testIdentificationOfAllFiles() throws Exception {

        for (String key : testFilesForIdentification.keySet()) {

            System.out.printf("Identifying '%s'\n", key);

            InputStream inputStream = TransmissionTestModule.class.getClassLoader().getResourceAsStream(key);
            assertNotNull(inputStream, "Unable to load '" + key + "' from classpath");

            TransmissionRequestBuilder requestBuilder = overridableTransmissionRequestBuilderCreator.createTansmissionRequestBuilderAllowingOverrides();
            requestBuilder.savePayLoad(inputStream);
            requestBuilder.overrideEndpointForStartProtocol(new URL("https://ap-test.unit4.com/override/trick/to/preventSMPLookup"));
            TransmissionRequest request = requestBuilder.build();

            PeppolStandardBusinessHeader facit = testFilesForIdentification.get(key);
            PeppolStandardBusinessHeader found = request.getPeppolStandardBusinessHeader();

            assertEquals(found.getDocumentTypeIdentifier(), facit.getDocumentTypeIdentifier());
            assertEquals(found.getProfileTypeIdentifier(), facit.getProfileTypeIdentifier());
            assertEquals(found.getSenderId(), facit.getSenderId());
            assertEquals(found.getRecipientId(), facit.getRecipientId());

        }

    }

    /**
     * Test that we allow various non UBL document types, as long as they have explicit set mandatory PEPPOL
     * headers : type, profile, customization, version, sender and receiver.
     */
    @Test
    public void testIdentificationOfNonUBLFiles() throws Exception {

        for (String key : testNonUBLFiles.keySet()) {

            System.out.printf("Accepting '%s'\n", key);

            InputStream inputStream = TransmissionTestModule.class.getClassLoader().getResourceAsStream(key);
            assertNotNull(inputStream, "Unable to load '" + key + "' from classpath");

            PeppolStandardBusinessHeader facit = testNonUBLFiles.get(key);

            TransmissionRequestBuilder requestBuilder = overridableTransmissionRequestBuilderCreator.createTansmissionRequestBuilderAllowingOverrides();
            requestBuilder.savePayLoad(inputStream);
            requestBuilder
                    .overrideEndpointForStartProtocol(new URL("https://ap-test.unit4.com/override/trick/to/preventSMPLookup"))
                    .receiver(facit.getRecipientId())
                    .sender(facit.getSenderId())
                    .documentType(facit.getDocumentTypeIdentifier())
                    .processType(facit.getProfileTypeIdentifier());

            TransmissionRequest request = requestBuilder.build();

            // the build() process should not have overridden any values we provided
            PeppolStandardBusinessHeader found = request.getPeppolStandardBusinessHeader();
            assertEquals(found.getDocumentTypeIdentifier(), facit.getDocumentTypeIdentifier());
            assertEquals(found.getProfileTypeIdentifier(), facit.getProfileTypeIdentifier());
            assertEquals(found.getSenderId(), facit.getSenderId());
            assertEquals(found.getRecipientId(), facit.getRecipientId());

        }

    }

}
