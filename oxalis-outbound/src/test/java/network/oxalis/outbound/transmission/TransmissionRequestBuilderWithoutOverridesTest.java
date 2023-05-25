/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import network.oxalis.api.lang.OxalisException;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.commons.guice.GuiceModuleLoader;
import network.oxalis.sniffer.identifier.ParticipantId;
import network.oxalis.test.lookup.MockLookupModule;
import network.oxalis.vefa.peppol.common.model.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.cert.X509Certificate;

import static org.testng.Assert.*;

/**
 * These tests needs TransmissionRequestBuilder to run in PRODUCTION-mode
 * and verifies that we are unable to override key values
 *
 * @author thore
 */
@Guice(modules = GuiceModuleLoader.class)
public class TransmissionRequestBuilderWithoutOverridesTest {

    @Inject
    @Named("sample-xml-with-sbdh")
    private InputStream inputStreamWithSBDH;

    @Inject
    @Named("sample-xml-no-sbdh")
    private InputStream noSbdhInputStream;

    @Inject
    private TransmissionRequestBuilder transmissionRequestBuilder;

    @Inject
    private X509Certificate certificate;


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

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = ".*not allowed to override \\[SenderId\\] in production mode.*")
    public void makeSureWeAreUnableToOverrideSender() throws OxalisException {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.sender(ParticipantIdentifier.of("0088:0000000000"));
        transmissionRequestBuilder.build();
        fail("We expected this test to fail");
    }

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = ".*not allowed to override \\[RecipientId\\] in production mode.*")
    public void makeSureWeAreUnableToOverrideReceiver() throws OxalisException {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.receiver(ParticipantIdentifier.of("0088:0000000000"));
        transmissionRequestBuilder.build();
        fail("We expected this test to fail");
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = ".*not allowed to override \\[DocumentTypeIdentifier\\] in production mode.*")
    public void makeSureWeAreUnableToOverrideDocumentType() throws OxalisException {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.documentType(DocumentTypeIdentifier.of("this::is##not::found"));
        transmissionRequestBuilder.build();
        fail("We expected this test to fail");
    }

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = ".*not allowed to override \\[ProfileTypeIdentifier\\] in production mode.*")
    public void makeSureWeAreUnableToOverrideProcessType() throws OxalisException {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.processType(ProcessIdentifier.of("urn:some-undefined-process"));
        transmissionRequestBuilder.build();
    }

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = ".*not allowed to override \\[SenderId, RecipientId, DocumentTypeIdentifier, ProfileTypeIdentifier\\] in production mode.*")
    public void makeSureWeDetectAllIllegalOverrides() throws OxalisException {
        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.sender(ParticipantIdentifier.of("0088:0000000000"));
        transmissionRequestBuilder.receiver(ParticipantIdentifier.of("0088:0000000000"));
        transmissionRequestBuilder.documentType(DocumentTypeIdentifier.of("this::is##not::found"));
        transmissionRequestBuilder.processType(ProcessIdentifier.of("urn:some-undefined-process"));
        transmissionRequestBuilder.build();
    }

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "You are not allowed to override the EndpointAddress from SMP.")
    public void makeSureWeDetectEndpointOverrides() throws Exception {
        MockLookupModule.resetService();

        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.overrideAs2Endpoint(Endpoint.of(
                TransportProfile.AS2_1_0, URI.create("http://localhost:8443/oxalis/as2"), null));
        transmissionRequestBuilder.build();
    }

    @Test
    public void makeSureWeCanSupplySameValuesAsThoseFromTheDocument() throws Exception {

        transmissionRequestBuilder.payLoad(inputStreamWithSBDH);
        transmissionRequestBuilder.sender(ParticipantIdentifier.of("0192:976098897"));
        transmissionRequestBuilder.receiver(ParticipantIdentifier.of("0192:810017902"));
        transmissionRequestBuilder.documentType(DocumentTypeIdentifier.of(
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol4a:ver1.0" +
                        "::2.0", DocumentTypeIdentifier.BUSDOX_DOCID_QNS_SCHEME));
        transmissionRequestBuilder.processType(ProcessIdentifier.of("urn:www.cenbii.eu:profile:bii04:ver1.0"));
        transmissionRequestBuilder.overrideAs2Endpoint(Endpoint.of(
                TransportProfile.AS2_1_0, URI.create("https://localhost:8080/oxalis/as2"), certificate));

        transmissionRequestBuilder.setTransmissionBuilderOverride(true);

        // Builds the request
        TransmissionRequest request = transmissionRequestBuilder.build();

        Header header = request.getHeader();
        assertNotEquals(header.getIdentifier().getIdentifier(), "1070e7f0-3bae-11e3-aa6e-0800200c9a66");
        assertEquals(header.getSender(), new ParticipantId("0192:976098897").toVefa());
        assertEquals(header.getReceiver(), new ParticipantId("0192:810017902").toVefa());
        assertEquals(header.getDocumentType(), DocumentTypeIdentifier.of(
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol4a:ver1.0" +
                        "::2.0", DocumentTypeIdentifier.BUSDOX_DOCID_QNS_SCHEME));
        assertEquals(header.getProcess(), ProcessIdentifier.of("urn:www.cenbii.eu:profile:bii04:ver1.0"));
        assertEquals(request.getEndpoint(), Endpoint.of(
                TransportProfile.AS2_1_0, URI.create("https://localhost:8080/oxalis/as2"), certificate));
    }

}
