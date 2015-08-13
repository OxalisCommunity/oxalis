/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.persistence;

import eu.peppol.PeppolMessageMetaData;
import org.w3c.dom.Document;
import java.io.InputStream;

/**
 * Repository of messages received.
 *
 * The access point will instantiate one object implementing this interface once and only once upon initialization.
 * If no custom implementations are found using the service locator, the built-in SimpleMessageRepository will be used.
 *
 * Remember to use an empty constructor in your own implementation.
 *
 * <p>Implementations are required to be thread safe.</p>
 *
 * @author Steinar Overbeck Cook
 * @author Thore Johnsen
 */
public interface MessageRepository {

    /**
     * Saves the supplied message after successful reception, using the given arguments.
     * This method is used when we have the XML Document, as we do in the START implementation.
     *
     * @param peppolMessageMetaData represents the message headers used for routing
     * @param document represents the message received, which should be persisted.
     * @throws OxalisMessagePersistenceException if persistence fails for some reason or another
     */
    public void saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, Document document) throws OxalisMessagePersistenceException;

    /**
     * Saves the supplied message after successful reception, using the given arguments.
     * This method is used when we have a streamed payload, as we do in the AS2 implementation.
     *
     * @param peppolMessageMetaData represents the message headers used for routing
     * @param payload represents the payload received, which should be persisted
     * @throws OxalisMessagePersistenceException if persistence fails for some reason or another
     */
    public void saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, InputStream payload) throws OxalisMessagePersistenceException;

}
