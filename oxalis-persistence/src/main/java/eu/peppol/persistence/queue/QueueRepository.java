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

package eu.peppol.persistence.queue;

import java.util.List;

public interface QueueRepository {

    /**
     * Updates the queue status to IN_PROGRESS so that the message will be removed
     * from the inbox/outbox. This is used as apart of an optimistic locking strategy
     *
     * @param outboundMessageQueueID the id of the queue to update
     * @return true if the queue item  was locked for delivery (i.e. the update of the row in the db was successful)
     */
    boolean lockQueueItemForDelivery(OutboundMessageQueueId outboundMessageQueueID);

    /**
     * Creates an entry in outbound_message_queue
     *
     * @return primary_key
     * @param msgNo
     */
    OutboundMessageQueueId putMessageOnQueue(Long msgNo);

    /**
     * Grabs the next bulk of messages waiting in the oubound queue.
     * Max bulk size can be specified using the returnLimit
     *
     * @param returnLimit specify max number of messages to return (0 for no limit)
     * @return list of messages ready to send
     */
    List<QueuedOutboundMessage> getQueuedMessages(long returnLimit);

    QueuedOutboundMessage getQueuedMessageById(OutboundMessageQueueId outboundQueueID);

    void changeQueuedMessageState(OutboundMessageQueueId outboundQueueID, OutboundMessageQueueState state);

    OutboundMessageQueueErrorId logOutboundError(QueuedOutboundMessageError error);

}
