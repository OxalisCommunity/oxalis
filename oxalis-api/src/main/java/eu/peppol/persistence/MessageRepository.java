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

import eu.peppol.PeppolMessageMetaData;
import eu.peppol.evidence.TransmissionEvidence;

import java.io.InputStream;

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
     * @param peppolMessageMetaData represents the message headers used for routing
     * @param payload               represents the payload received, which should be persisted
     * @throws OxalisMessagePersistenceException if persistence fails for some reason or another
     */
    public void saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, InputStream payload) throws OxalisMessagePersistenceException;


    /**
     * Saves a generic transport receipt to persistent storage. This is typically used in C3 to persist the transport receipt
     * being returned to C2. In C2, which is the sending Access Point, this generic receipt must be saved to persistent storage as a proof of delivery.
     *
     * @param transmissionEvidence
     */
    void saveTransportReceipt(TransmissionEvidence transmissionEvidence, PeppolMessageMetaData peppolMessageMetaData);

    /**
     * Saves the native transport receipt to persistent storage. Typically the MDN for AS2
     *
     * @param bytes
     */
    void saveNativeTransportReceipt(byte[] bytes);
}
