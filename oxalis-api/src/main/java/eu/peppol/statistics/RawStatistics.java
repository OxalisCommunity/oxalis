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
