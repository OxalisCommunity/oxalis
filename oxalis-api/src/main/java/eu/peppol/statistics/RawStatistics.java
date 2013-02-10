package eu.peppol.statistics;

import eu.peppol.start.identifier.*;

import java.util.Date;

/** Holds a raw statistics entry.
 *
 * User: steinar
 * Date: 30.01.13
 * Time: 20:35
 */
public class RawStatistics {


    AccessPointIdentifier accessPointIdentifier;
    Date date;
    Direction direction;
    ParticipantId sender;
    ParticipantId receiver;
    PeppolDocumentTypeId peppolDocumentTypeId;
    ChannelId channelId;
    PeppolProcessTypeId peppolProcessTypeId;

    private RawStatistics(Builder builder) {
        this.accessPointIdentifier = builder.accessPointIdentifier;
        this.date = builder.date;
        this.direction = builder.direction;
        this.sender = builder.sender;
        this.receiver = builder.receiver;
        this.peppolDocumentTypeId = builder.peppolDocumentTypeId;
        this.peppolProcessTypeId = builder.peppolProcessTypeId;
        this.channelId = builder.channelId;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Direction {
        IN, OUT
    }

    public static class Builder {
        AccessPointIdentifier accessPointIdentifier;
        Date date = new Date();
        Direction direction;
        ParticipantId sender;
        ParticipantId receiver;
        PeppolDocumentTypeId peppolDocumentTypeId;
        PeppolProcessTypeId peppolProcessTypeId;
        ChannelId channelId;

        public Builder date(Date dt) {
            this.date = dt;
            return this;
        }

        public Builder accessPointIdentifier(AccessPointIdentifier accessPointIdentifier) {
            this.accessPointIdentifier = accessPointIdentifier;
            return this;
        }

        public Builder OUT() {
            this.direction = Direction.OUT;
            return this;
        }

        public Builder IN() {
            this.direction = Direction.IN;
            return this;
        }

        public Builder sender(ParticipantId sender) {
            this.sender = sender;
            return this;
        }

        public Builder receiver(ParticipantId receiver) {
            this.receiver = receiver;
            return this;
        }

        public Builder documentType(PeppolDocumentTypeId peppolDocumentTypeId) {
            this.peppolDocumentTypeId = peppolDocumentTypeId;
            return this;
        }

        public Builder profile(PeppolProcessTypeId peppolProcessTypeId) {
            this.peppolProcessTypeId = peppolProcessTypeId;
            return this;
        }

        public Builder channel(ChannelId channelId) {
            this.channelId = channelId;
            return  this;
        }

        public RawStatistics build() {

            if (direction == null) {
                throw new IllegalStateException("Must specify the direction of the message");
            }
            if (sender == null) {
                throw new IllegalStateException("Must specify identity of sender");
            }
            if (receiver == null) {
                throw new IllegalStateException("Identity of receiver required");
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

            return new RawStatistics(this);
        }
    }



    public Date getDate() {
        return date;
    }

    public AccessPointIdentifier getAccessPointIdentifier() {
        return accessPointIdentifier;
    }

    public ParticipantId getSender() {
        return sender;
    }

    public ParticipantId getReceiver() {
        return receiver;
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
}
