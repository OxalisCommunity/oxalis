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

import network.oxalis.statistics.api.RawStatistics;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;

/**
 * Holds a raw statistics entry, which represents a single receipt or transmit operation.
 * <p>
 * User: steinar
 * Date: 30.01.13
 * Time: 20:35
 */
public class DefaultRawStatistics extends AbstractStatistics implements RawStatistics {

    protected ParticipantIdentifier sender;

    protected ParticipantIdentifier receiver;

    private DefaultRawStatistics(RawStatisticsBuilder builder) {
        super(builder);
        this.sender = builder.sender;
        this.receiver = builder.receiver;

    }

    @Override
    public ParticipantIdentifier getSender() {
        return sender;
    }

    @Override
    public ParticipantIdentifier getReceiver() {
        return receiver;
    }

    public static class RawStatisticsBuilder
            extends AbstractStatistics.AbstractBuilder<RawStatisticsBuilder, DefaultRawStatistics> {

        ParticipantIdentifier sender;

        ParticipantIdentifier receiver;

        public RawStatisticsBuilder sender(ParticipantIdentifier sender) {
            this.sender = sender;
            return getThis();
        }

        public RawStatisticsBuilder receiver(ParticipantIdentifier receiver) {
            this.receiver = receiver;
            return getThis();
        }

        public DefaultRawStatistics build() {

            checkRequiredFields();

            if (sender == null) {
                throw new IllegalStateException("Must specify identity of sender");
            }
            if (receiver == null) {
                throw new IllegalStateException("Identity of receiver required");
            }

            return new DefaultRawStatistics(this);
        }

        @Override
        protected RawStatisticsBuilder getThis() {
            return this;
        }
    }
}
