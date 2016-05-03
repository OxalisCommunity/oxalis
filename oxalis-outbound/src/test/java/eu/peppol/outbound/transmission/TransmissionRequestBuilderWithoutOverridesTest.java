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

package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.smp.SmpLookupManager;
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
@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class})
public class TransmissionRequestBuilderWithoutOverridesTest {

    @Inject @Named("sample-xml-with-sbdh")
    InputStream inputStreamWithSBDH;

    @Inject @Named("sample-xml-no-sbdh")
    InputStream noSbdhInputStream;

    @Inject
    TransmissionRequestBuilder transmissionRequestBuilder;


    @BeforeMethod
    public void setUp() {
        // Defaults to prevention of overriding
        transmissionRequestBuilder.setTransmissionBuilderOverride(false);

        // Ensures that the state of the transmissionrequest builder is reset for each test method
        transmissionRequestBuilder.reset();
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

    @Test(expectedExceptions = RuntimeException.class,
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

    @Test
    public void makeSureWeCanSupplySameValuesAsThoseFromTheDocument() throws Exception {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.messageId(new MessageId("1070e7f0-3bae-11e3-aa6e-0800200c9a66"));
        transmissionRequestBuilder.sender(new ParticipantId("9908:976098897"));
        transmissionRequestBuilder.receiver(new ParticipantId("9908:810017902"));
        transmissionRequestBuilder.documentType(PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0"));
        transmissionRequestBuilder.processType(PeppolProcessTypeId.valueOf("urn:www.cenbii.eu:profile:bii04:ver1.0"));
        transmissionRequestBuilder.overrideAs2Endpoint(new URL("https://localhost:8080/oxalis/as2"), null);

        transmissionRequestBuilder.setTransmissionBuilderOverride(true);
        TransmissionRequest request = transmissionRequestBuilder.build();
        PeppolStandardBusinessHeader sbdh = request.getPeppolStandardBusinessHeader();
        assertEquals(sbdh.getMessageId().toString(), "1070e7f0-3bae-11e3-aa6e-0800200c9a66");
        assertEquals(sbdh.getSenderId(), new ParticipantId("9908:976098897"));
        assertEquals(sbdh.getRecipientId(), new ParticipantId("9908:810017902"));
        assertEquals(sbdh.getDocumentTypeIdentifier(), PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0"));
        assertEquals(sbdh.getProfileTypeIdentifier(), PeppolProcessTypeId.valueOf("urn:www.cenbii.eu:profile:bii04:ver1.0"));
        assertEquals(request.getEndpointAddress(), new SmpLookupManager.PeppolEndpointData(new URL("https://localhost:8080/oxalis/as2"), BusDoxProtocol.AS2));
    }

}
