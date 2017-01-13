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

package eu.peppol.persistence;

import eu.peppol.PeppolTransmissionMetaData;
import eu.peppol.evidence.TransmissionEvidence;
import eu.peppol.identifier.MessageId;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Repository of messages received.
 * <p>
 * The access point will instantiate one object implementing this interface once and only once upon initialization.
 * If no custom implementations are found using the service locator, the built-in {@code SimpleMessageRepository} will be used.
 * <p>
 * Remember to use an empty constructor in your own implementation.
 * <p>
 * <p>Implementations are required to be thread safe.</p>
 *
 * @author Steinar Overbeck Cook
 * @author Thore Johnsen
 */
public interface MessageRepository {

    /**
     * Saves the supplied message after successful reception, using the given arguments.
     * This method is used when we have a streamed payload, as we do in the AS2 implementation.
     *
     * @param peppolTransmissionMetaData represents the message headers used for routing
     * @param payload               represents the payload received, which should be persisted
     * @throws OxalisMessagePersistenceException if persistence fails for some reason or another
     */
    Long saveInboundMessage(PeppolTransmissionMetaData peppolTransmissionMetaData, InputStream payload) throws OxalisMessagePersistenceException;


    Long saveOutboundMessage(MessageMetaData messageMetaData, InputStream payloadDocument) throws OxalisMessagePersistenceException;

    Long saveOutboundMessage(MessageMetaData messageMetaData, Document payloadDocument) throws OxalisMessagePersistenceException;

    Long saveInboundMessage(MessageMetaData messageMetaData, InputStream payload) throws OxalisMessagePersistenceException;


    /**
     * Saves a generic transport receipt to persistent storage. This is typically used in C3 to persist the transport receipt
     * being returned to C2. In C2, which is the sending Access Point, this generic receipt must be saved to persistent storage as a proof of delivery.
     *
     * @param transmissionEvidence
     */
    void saveInboundTransportReceipt(TransmissionEvidence transmissionEvidence, PeppolTransmissionMetaData peppolTransmissionMetaData) throws OxalisMessagePersistenceException;

    void saveOutboundTransportReceipt(TransmissionEvidence transmissionEvidence, MessageId messageId) throws OxalisMessagePersistenceException;

    MessageMetaData findByMessageNo(Long msgNo);

    /**
     * Find an instance of {@link MessageMetaData} by {@link TransferDirection} and {@link MessageId}, i.e. the UUID assigned when we receive a message either
     * from PEPPOL or our back end.
     * The combination of arguments {@link TransferDirection} and {@link MessageId} ensures that messages sent and received by this access point are unique.
     *
     * @param transferDirection indicates whether the message is inbound or outbound.
     * @param messageId the key
     * @return an instance of {@link MessageMetaData} populated with data from the repository (DBMS)
     * @throws IllegalStateException if a message with the given MessageId does not exist
     */
    Optional<MessageMetaData> findByMessageId(TransferDirection transferDirection, MessageId messageId) throws IllegalStateException;

    List<MessageMetaData> findByMessageId(MessageId messageId);

    /**
     *
     */
}
