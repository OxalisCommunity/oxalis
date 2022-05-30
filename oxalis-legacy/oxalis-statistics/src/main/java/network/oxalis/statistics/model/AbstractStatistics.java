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

package network.oxalis.statistics.model;

import network.oxalis.api.model.AccessPointIdentifier;
import network.oxalis.api.model.Direction;
import network.oxalis.statistics.api.ChannelId;
import network.oxalis.statistics.api.RawStatistics;
import network.oxalis.vefa.peppol.common.model.DocumentTypeIdentifier;
import network.oxalis.vefa.peppol.common.model.ProcessIdentifier;

import java.util.Date;

/**
 * Basic collection of statistics held for a given transmission
 *
 * @author steinar
 *         Date: 25.03.13
 *         Time: 14:09
 */
abstract class AbstractStatistics implements RawStatistics {

    AccessPointIdentifier accessPointIdentifier;

    Date date;

    Direction direction;

    DocumentTypeIdentifier peppolDocumentTypeId;

    ChannelId channelId;

    ProcessIdentifier processIdentifier;

    AbstractStatistics(AbstractBuilder abstractBuilder) {
        this.processIdentifier = abstractBuilder.peppolProcessTypeId;
        this.peppolDocumentTypeId = abstractBuilder.peppolDocumentTypeId;
        this.accessPointIdentifier = abstractBuilder.accessPointIdentifier;
        this.date = abstractBuilder.date;
        this.direction = abstractBuilder.direction;
        this.channelId = abstractBuilder.channelId;
    }

    public Direction getDirection() {
        return direction;
    }

    public Date getDate() {
        return date;
    }

    public AccessPointIdentifier getAccessPointIdentifier() {
        return accessPointIdentifier;
    }

    public DocumentTypeIdentifier getDocumentTypeIdentifier() {
        return peppolDocumentTypeId;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public ProcessIdentifier getProcessIdentifier() {
        return processIdentifier;
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder, B> {

        AccessPointIdentifier accessPointIdentifier;

        Date date = new Date();

        Direction direction;

        DocumentTypeIdentifier peppolDocumentTypeId;

        ProcessIdentifier peppolProcessTypeId;

        ChannelId channelId;

        public T date(Date dt) {
            this.date = dt;
            return getThis();
        }

        public T accessPointIdentifier(AccessPointIdentifier accessPointIdentifier) {
            this.accessPointIdentifier = accessPointIdentifier;
            return getThis();
        }


        public T direction(Direction direction) {
            this.direction = direction;
            return getThis();
        }

        public T outbound() {
            this.direction = Direction.OUT;
            return getThis();
        }

        public T inbound() {
            this.direction = Direction.IN;
            return getThis();
        }

        public T documentType(DocumentTypeIdentifier peppolDocumentTypeId) {
            this.peppolDocumentTypeId = peppolDocumentTypeId;
            return getThis();
        }

        public T profile(ProcessIdentifier processIdentifier) {
            this.peppolProcessTypeId = processIdentifier;
            return getThis();
        }

        public T channel(ChannelId channelId) {
            this.channelId = channelId;
            return getThis();
        }

        protected void checkRequiredFields() {

            if (direction == null) {
                throw new IllegalStateException("Must specify the direction of the message");
            }

            if (accessPointIdentifier == null) {
                throw new IllegalStateException("Identity of access point required");
            }

            if (peppolDocumentTypeId == null) {
                throw new IllegalStateException("Document type required");
            }
            if (peppolProcessTypeId == null) {
                throw new IllegalStateException("Process id/profile id required");
            }

            if (date == null) {
                throw new IllegalStateException("Date (period) required");
            }
        }

        public abstract B build();

        protected abstract T getThis();
    }
}
