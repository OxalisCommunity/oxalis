package eu.peppol.statistics;

import eu.peppol.start.identifier.ParticipantId;

/** Holds aggregated statistics entry.
 *
 * @author steinar
 *         Date: 25.03.13
 *         Time: 14:46
 */
public class AggregatedStatistics extends AbstractStatistics {

    ParticipantId participantId;
    Integer count;

    AggregatedStatistics(Builder builder) {
        super(builder);

        this.participantId = builder.participantId;
        this.count = builder.count;
    }

    public static class Builder extends AbstractBuilder<Builder, AggregatedStatistics> {

        ParticipantId participantId;
        Integer count;

        public Builder participantId(ParticipantId participantId) {
            this.participantId = participantId;
            return getThis();
        }

        public Builder count(Integer count) {
            this.count = count;
            return getThis();
        }

        @Override
        public AggregatedStatistics build() {
            checkRequiredFields();

            if (participantId == null) {
                throw new IllegalStateException("ParticipantId is required property");
            }

            if (count == null) {
                throw new IllegalStateException("count is required property");
            }
            return new AggregatedStatistics(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }

}
