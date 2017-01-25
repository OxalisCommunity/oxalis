/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.peppol.statistics;

import eu.peppol.identifier.ParticipantId;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;

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

        public RawStatisticsBuilder sender(ParticipantIdentifier sender) {
            this.sender = new ParticipantId(sender.getIdentifier());
            return getThis();
        }

        public RawStatisticsBuilder receiver(ParticipantIdentifier receiver) {
            this.receiver = new ParticipantId(receiver.getIdentifier());
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
