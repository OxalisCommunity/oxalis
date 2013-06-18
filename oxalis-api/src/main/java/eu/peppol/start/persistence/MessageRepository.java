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

package eu.peppol.start.persistence;

import eu.peppol.start.identifier.PeppolMessageHeader;
import org.w3c.dom.Document;

/**
 * Repository of messages received.
 * The access point will invoke objects implementing this interface once and only once upon
 * initialization.
 * <p>Implementations are required to be thread safe.</p>
 * 
 * @author Steinar Overbeck Cook
 *
 *         Created by
 *         User: steinar
 *         Date: 28.11.11
 *         Time: 20:55
 */
    public interface MessageRepository {


    /**
     * Saves the supplied message after successful reception, using the given arguments.
     * @param inboundMessageStore the full path to the directory in which the inbound messages should be stored. The value of this parameter is specified by
     *                            the property <code>oxalis.inbound.message.store</code> and <code>oxalis.outbound.message.store</code>, which may be configured
     *                            either as a system property, in <code>oxalis.properties</code> or <code>oxalis-web.properties</code>
     *
     * @param peppolMessageHeader represents the message headers used for routing
     * @param document represents the message received, which should be persisted.
     *
     */
    public boolean saveInboundMessage(String inboundMessageStore, PeppolMessageHeader peppolMessageHeader, Document document);

}
