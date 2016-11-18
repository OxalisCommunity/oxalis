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
import eu.peppol.as2.As2Module;
import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.persistence.MessageMetaData;
import eu.peppol.persistence.MessageRepository;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Verifies that class Transmitter works as expected.
 *
 * Requires that Oxalis is running on the local port.
 *
 * @author steinar
 *         Date: 17.11.2016
 *         Time: 15.20
 */
@Test(groups = {"integration"})
@Guice(modules = {TransmissionTestITModule.class, As2Module.class })
public class TransmitterTestIT {

    @Inject
    Transmitter transmitter;

    @Inject
    TransmissionRequestBuilder transmissionRequestBuilder;

    @Inject
    MessageRepository messageRepository;

    @Inject @Named("sample-ehf-invoice-no-sbdh")
    InputStream inputStream;

    @Test
    public void testTransmit() throws Exception {

        MessageId messageId = new MessageId();
        TransmissionRequest transmissionRequest = transmissionRequestBuilder.messageId(messageId)
                .sender(WellKnownParticipant.U4_TEST)
                .receiver(WellKnownParticipant.U4_TEST)
                .payLoad(inputStream)
                .build();

        TransmissionResponse transmissionResponse = transmitter.transmit(transmissionRequest);

        // The messageId should not have changed
        assertEquals(transmissionResponse.getStandardBusinessHeader().getMessageId(), messageId);

        // Let's inspect the database as well
        MessageMetaData messageMetaData = messageRepository.findByMessageId(messageId);
        assertNotNull(messageMetaData,"Message with messageId=" + messageId + " not found in database");
    }
}