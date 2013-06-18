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

package eu.peppol.inbound.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.peppol.start.identifier.PeppolMessageHeader;
import eu.peppol.start.persistence.MessageRepository;
import org.w3c.dom.Document;

/**
 * @author steinar
 *         Date: 09.06.13
 *         Time: 23:57
 */
public class TestRepositoryModule extends AbstractModule {


    private final MessageRepository messageRepository;

    public TestRepositoryModule(MessageRepository messageRepository) {

        this.messageRepository = messageRepository;
    }

    @Override
    protected void configure() {

    }

    @Provides
    MessageRepository provideMessageRepository() {
        return messageRepository;
    }
}
