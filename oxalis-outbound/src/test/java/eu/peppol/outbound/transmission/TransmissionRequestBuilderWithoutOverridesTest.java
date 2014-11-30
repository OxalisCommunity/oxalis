package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import eu.peppol.identifier.*;
import eu.peppol.outbound.guice.TestResourceModule;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.testng.Assert.*;

/**
 * These tests needs TransmissionRequestBuilder to run in PRODUCTION-mode
 * and verifies that we are unable to override key values
 *
 * @author thore
 */
@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class })
public class TransmissionRequestBuilderWithoutOverridesTest {

    @Inject
    Injector injector;

    @Inject @Named("sample-xml-with-sbdh")
    InputStream inputStreamWithSBDH;

    @Inject @Named("sample-xml-no-sbdh")
    InputStream noSbdhInputStream;

    TransmissionRequestBuilder transmissionRequestBuilder;

    @BeforeMethod
    public void setUp() {
        transmissionRequestBuilder = injector.getInstance(TransmissionRequestBuilder.class);
        inputStreamWithSBDH.mark(Integer.MAX_VALUE);
        noSbdhInputStream.mark(Integer.MAX_VALUE);
    }

    @AfterMethod
    public void tearDown() throws IOException {
        inputStreamWithSBDH.reset();
        noSbdhInputStream.reset();
    }

    @Test
    public void makeSureWeDoNotAllowOverrides() {
        assertNotNull(transmissionRequestBuilder);
        assertFalse(transmissionRequestBuilder.isOverrideAllowed());
    }

    @Test
    public void makeSureTestDataAreAvailable() {
        assertNotNull(inputStreamWithSBDH);
        assertNotNull(noSbdhInputStream);
    }

    @Test
    public void makeSureWeCanOverrideMessageId() {
        MessageId newMessageId = new MessageId("this is our new message id");
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.messageId(newMessageId);
        TransmissionRequest transmissionRequest = transmissionRequestBuilder.build();
        assertEquals(transmissionRequest.getPeppolStandardBusinessHeader().getMessageId(), newMessageId);
    }

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp=".*not allowed to override \\[SenderId\\] in production mode.*")
    public void makeSureWeAreUnableToOverrideSender() {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.sender(new ParticipantId("0088:0000000000"));
        transmissionRequestBuilder.build();
        fail("We expected this test to fail");
    }

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp=".*not allowed to override \\[RecipientId\\] in production mode.*")
    public void makeSureWeAreUnableToOverrideReceiver() {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.receiver(new ParticipantId("0088:0000000000"));
        transmissionRequestBuilder.build();
        fail("We expected this test to fail");
    }

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp=".*not allowed to override \\[DocumentTypeIdentifier\\] in production mode.*")
    public void makeSureWeAreUnableToOverrideDocumentType() {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.documentType(PeppolDocumentTypeId.valueOf("this::is##not::found"));
        transmissionRequestBuilder.build();
        fail("We expected this test to fail");
    }

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp=".*not allowed to override \\[ProfileTypeIdentifier\\] in production mode.*")
    public void makeSureWeAreUnableToOverrideProcessType() {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.processType(PeppolProcessTypeId.valueOf("urn:some-undefined-process"));
        transmissionRequestBuilder.build();
        fail("We expected this test to fail");
    }

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp=".*not allowed to override \\[SenderId, RecipientId, DocumentTypeIdentifier, ProfileTypeIdentifier\\] in production mode.*")
    public void makeSureWeDetectAllIllegalOverrides() {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.messageId(new MessageId("some-id"));
        transmissionRequestBuilder.sender(new ParticipantId("0088:0000000000"));
        transmissionRequestBuilder.receiver(new ParticipantId("0088:0000000000"));
        transmissionRequestBuilder.documentType(PeppolDocumentTypeId.valueOf("this::is##not::found"));
        transmissionRequestBuilder.processType(PeppolProcessTypeId.valueOf("urn:some-undefined-process"));
        transmissionRequestBuilder.build();
        fail("We expected this test to fail");
    }

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp="You are not allowed to override the EndpointAddress from SMP in production mode.")
    public void makeSureWeDetectEndpointOverrides() throws Exception {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.overrideAs2Endpoint(new URL("http://localhost:8443/oxalis/as2"), "some-illegal-common-name");
        transmissionRequestBuilder.build();
        fail("We expected this test to fail");
    }

}
