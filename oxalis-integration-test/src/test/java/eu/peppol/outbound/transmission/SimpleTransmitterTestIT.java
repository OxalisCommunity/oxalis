/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

import com.google.inject.name.Named;
import eu.peppol.as2.inbound.As2InboundModule;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.persistence.MessageMetaData;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.file.ArtifactPathComputer;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.Transmitter;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static eu.peppol.persistence.TransferDirection.IN;
import static eu.peppol.persistence.TransferDirection.OUT;
import static org.testng.Assert.*;

/**
 * Verifies that class Transmitter works as expected.
 * <p>
 * Requires that Oxalis is running on the local port.
 *
 * @author steinar
 *         Date: 17.11.2016
 *         Time: 15.20
 */
@Test(groups = {"integration"})
@Guice(modules = {TransmissionTestITModule.class, As2InboundModule.class})
public class SimpleTransmitterTestIT {

    @Inject
    @Named("simple")
    Transmitter transmitter;

    @Inject
    TransmissionRequestBuilder transmissionRequestBuilder;

    @Inject
    MessageRepository messageRepository;

    @Inject
    @Named("sample-ehf-invoice-no-sbdh")
    InputStream inputStream;

    @Inject
    ArtifactPathComputer artifactPathComputer;

    @Test
    public void testTransmit() throws Exception {

        TransmissionRequest transmissionRequest = transmissionRequestBuilder
                .sender(WellKnownParticipant.U4_TEST)
                .receiver(WellKnownParticipant.U4_TEST)
                .payLoad(inputStream)
                .build();

        TransmissionResponse transmissionResponse = transmitter.transmit(transmissionRequest);

        // Let's inspect the database as well
        Optional<MessageMetaData> messageMetaDataOptional = messageRepository.findByMessageId(OUT, transmissionResponse.getMessageId());
        assertFalse(messageMetaDataOptional.isPresent(), "Nothing should be in the database when using the simple transmitter");

        // However, the inbound receiver should definetely store something in the database
        messageMetaDataOptional = messageRepository.findByMessageId(IN, transmissionResponse.getMessageId());
        assertTrue(messageMetaDataOptional.isPresent(), "The inbound receiver has not stored anything in the database");

        MessageMetaData messageMetaData = messageMetaDataOptional.get();

        assertNotNull(messageMetaData.getPayloadUri(), "Payload URI is empty");
        assertNotNull(messageMetaData.getNativeEvidenceUri(), "Native evidence uri is empty");

        for (URI uri : new URI[]{messageMetaData.getPayloadUri(), messageMetaData.getNativeEvidenceUri()}) {
            // Ensures that it exists
            assertTrue(Files.isReadable(Paths.get(uri)));

            // Verify the path starts correctly
            assertTrue(Paths.get(uri).startsWith(artifactPathComputer.createBasePath(IN)));
        }
    }

    @Test
    public void testPathManipulation() {
        Path path = Paths.get(URI.create("file:///var/peppol/IN/9908_810017902"));
        Path p2 = Paths.get(URI.create("file:///var/peppol/IN/9908_810017902/9908_810017902/2016-11-20/6365df80-6488-44af-941b-a7fd7d46fccb-rem.xml"));

        assertTrue(p2.startsWith(path));

    }
}