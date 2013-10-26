package eu.peppol.statistics;

import eu.peppol.identifier.ParticipantId;

/** Holds a raw statistics entry, which represents a single receipt or transmit operation.
 *
 * User: steinar
 * Date: 30.01.13
 * Time: 20:35
 */
public class RawStatistics extends AbstractStatistics {

    ParticipantId sender, receiver;

    private RawStatistics(RawStatisticsBuilder builder) {
        super(builder);
        this.sender = builder.sender;
        this.receiver = builder.receiver;

    }

    public ParticipantId getSender() {
        return sender;
    }

    public ParticipantId getReceiver() {
        return receiver;
    }

    public static class RawStatisticsBuilder extends AbstractStatistics.AbstractBuilder<RawStatisticsBuilder, RawStatistics> {
        ParticipantId sender;
        ParticipantId receiver;

        public RawStatisticsBuilder sender(ParticipantId sender) {
            this.sender = sender;
            return getThis();
        }

        public RawStatisticsBuilder receiver(ParticipantId receiver) {
            this.receiver = receiver;
            return getThis();
        }

        public RawStatistics build() {

            checkRequiredFields();

            if (sender == null) {
                throw new IllegalStateException("Must specify identity of sender");
            }
            if (receiver == null) {
                throw new IllegalStateException("Identity of receiver required");
            }

            return new RawStatistics(this);
        }

        @Override
        protected RawStatisticsBuilder getThis() {
            return this;
        }
    }


}
