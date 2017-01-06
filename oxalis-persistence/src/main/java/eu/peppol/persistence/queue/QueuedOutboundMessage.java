/*
 * Copyright (c) 2010 - 2017 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.persistence.queue;

import eu.peppol.persistence.MessageNumber;

/**
 * User: adam
 * Date: 16.03.13
 * Time: 16:03
 */
public class QueuedOutboundMessage {
    private final OutboundMessageQueueId outboundQueueID;
    private final MessageNumber messageNumber;
    private final OutboundMessageQueueState state;

    public QueuedOutboundMessage(OutboundMessageQueueId outboundQueueID, MessageNumber messageNumber, OutboundMessageQueueState state) {
        this.outboundQueueID = outboundQueueID;
        this.messageNumber = messageNumber;
        this.state = state;
    }

    public OutboundMessageQueueId getOutboundQueueId() {
        return outboundQueueID;
    }

    public MessageNumber getMessageNumber() {
        return messageNumber;
    }

    public OutboundMessageQueueState getState() {
        return state;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueuedOutboundMessage that = (QueuedOutboundMessage) o;

        if (messageNumber != null ? !messageNumber.equals(that.messageNumber) : that.messageNumber != null)
            return false;
        if (outboundQueueID != null ? !outboundQueueID.equals(that.outboundQueueID) : that.outboundQueueID != null)
            return false;
        if (state != that.state) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = outboundQueueID != null ? outboundQueueID.hashCode() : 0;
        result = 31 * result + (messageNumber != null ? messageNumber.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

}
