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
import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeIdAcronym;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.persistence.AccountId;
import eu.peppol.persistence.ChannelProtocol;
import eu.peppol.persistence.MessageMetaData;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.api.UserName;
import eu.peppol.persistence.api.account.Account;
import eu.peppol.persistence.api.account.AccountRepository;
import eu.peppol.persistence.api.account.Customer;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.Transmitter;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
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
public class EvidencePersistingTransmitterTestIT {

    @Inject
    @Named("advanced")
    Transmitter transmitter;

    @Inject
    TransmissionRequestBuilder transmissionRequestBuilder;

    @Inject
    AccountRepository accountRepository;

    @Inject
    MessageRepository messageRepository;

    @Inject
    @Named("sample-ehf-invoice-no-sbdh")
    InputStream inputStream;

    @Test
    public void testTransmit() throws Exception {

        Customer customer = accountRepository.createCustomer("test", "test@acme.com", "123", "Norway", "Steinar", "Adr1", "adre2", "1472", "Fjellhamar", "976098897");
        Account account = accountRepository.createAccount(new Account(customer.getCustomerId(), "TestAccount", new UserName("buster"), new Date(), "secret", new AccountId(42), true, false), WellKnownParticipant.U4_TEST);


        MessageId messageId = new MessageId();

        // Creates an entry in the MESSAGE table representing the message to be sent.
        MessageMetaData.Builder builder = new MessageMetaData.Builder(OUT,
                WellKnownParticipant.U4_TEST,
                WellKnownParticipant.U4_TEST,
                PeppolDocumentTypeIdAcronym.EHF_INVOICE.getDocumentTypeIdentifier(), ChannelProtocol.SREST);

        MessageMetaData metaData = builder.messageId(messageId)
                .processTypeId(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId())
                .accountId(account.getAccountId())
                .build();


        Long messageNo = messageRepository.saveOutboundMessage(metaData, inputStream);

        // Loads the message back from the database again
        MessageMetaData messageMetaData = messageRepository.findByMessageNo(messageNo);

        assertNotNull(messageMetaData.getPayloadUri());
        Path path = Paths.get(messageMetaData.getPayloadUri());
        assertTrue(Files.exists(path), path + " does not exist");


        // Transmits the outbound message to the access point.
        InputStream payloadStream = Files.newInputStream(path);

        TransmissionRequest transmissionRequest = transmissionRequestBuilder
                .sender(WellKnownParticipant.U4_TEST)
                .receiver(WellKnownParticipant.U4_TEST)
                .payLoad(payloadStream)
                .build();

        TransmissionResponse transmissionResponse = transmitter.transmit(transmissionRequest);

        // ======== Verify the state of the outbound and the inbound message.

        // MessageId must not be mixed up with the SBDH instance identifier
        assertNotEquals(transmissionResponse.getStandardBusinessHeader().getInstanceId(), messageId);

        // Let's inspect the database as well
        Optional<MessageMetaData> metaDataOptional = messageRepository.findByMessageId(OUT, messageId);
        assertTrue(metaDataOptional.isPresent());

        MessageMetaData mmdOut = metaDataOptional.get();
        assertNotNull(mmdOut.getDelivered(), "The delivered timestamp should have been set.");

        assertNotNull(mmdOut.getDelivered(), "Successfully delivered outbound message should have 'delivered' set");

        Optional<MessageMetaData> messageMetaDataOptionalInbound = messageRepository.findByMessageId(IN, messageId);
        assertTrue(messageMetaDataOptionalInbound.isPresent(), "No entry found in message table for inbound message");

        MessageMetaData mmdInbound = messageMetaDataOptionalInbound.get();
        assertNotNull(mmdInbound.getReceived());

    }
}