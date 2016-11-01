package eu.peppol.persistence.api;

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
