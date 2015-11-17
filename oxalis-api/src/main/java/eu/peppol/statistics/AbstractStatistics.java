package eu.peppol.statistics;

import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.start.identifier.*;

import java.util.Date;
import java.util.UUID;

/**
 * Basic collection of statistics held for a given transmission
 *
 * @author steinar
 *         Date: 25.03.13
 *         Time: 14:09
 */
class AbstractStatistics {

    AccessPointIdentifier accessPointIdentifier;
    Date date;
    Direction direction;
    PeppolDocumentTypeId peppolDocumentTypeId;
    ChannelId channelId;
    PeppolProcessTypeId peppolProcessTypeId;
    UUID uid;

    AbstractStatistics(AbstractBuilder abstractBuilder) {
        this.peppolProcessTypeId = abstractBuilder.peppolProcessTypeId;
        this.peppolDocumentTypeId = abstractBuilder.peppolDocumentTypeId;
        this.accessPointIdentifier = abstractBuilder.accessPointIdentifier;
        this.date = abstractBuilder.date;
        this.direction = abstractBuilder.direction;
        this.channelId = abstractBuilder.channelId;
        this.uid = abstractBuilder.uid;
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

    public PeppolDocumentTypeId getPeppolDocumentTypeId() {
        return peppolDocumentTypeId;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public PeppolProcessTypeId getPeppolProcessTypeId() {
        return peppolProcessTypeId;
    }

    public UUID getUid() {
        return uid;
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder, B> {
        AccessPointIdentifier accessPointIdentifier;
        Date date = new Date();
        Direction direction;
        PeppolDocumentTypeId peppolDocumentTypeId;
        PeppolProcessTypeId peppolProcessTypeId;
        ChannelId channelId;
        UUID uid;

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

        public T documentType(PeppolDocumentTypeId peppolDocumentTypeId) {
            this.peppolDocumentTypeId = peppolDocumentTypeId;
            return getThis();
        }

        public T profile(PeppolProcessTypeId peppolProcessTypeId) {
            this.peppolProcessTypeId = peppolProcessTypeId;
            return getThis();
        }

        public T channel(ChannelId channelId) {
            this.channelId = channelId;
            return getThis();
        }

        public T uid(UUID uid) {
            this.uid = uid;
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
