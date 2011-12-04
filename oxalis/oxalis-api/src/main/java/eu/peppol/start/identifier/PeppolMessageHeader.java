package eu.peppol.start.identifier;

/**
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 18:43
 */
public class PeppolMessageHeader {

    MessageId messageId;
    ChannelId channelId = new ChannelId(null);
    ParticipantId recipientId;
    ParticipantId senderId;
    DocumentId documentId;
    ProcessId processId;

    public MessageId getMessageId() {
        return messageId;
    }

    public void setMessageId(MessageId messageId) {
        this.messageId = messageId;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }

    public ParticipantId getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(ParticipantId recipientId) {
        this.recipientId = recipientId;
    }

    public ParticipantId getSenderId() {
        return senderId;
    }

    public void setSenderId(ParticipantId senderId) {
        this.senderId = senderId;
    }

    public DocumentId getDocumentId() {
        return documentId;
    }

    public void setDocumentId(DocumentId documentId) {
        this.documentId = documentId;
    }

    public ProcessId getProcessId() {
        return processId;
    }

    public void setProcessId(ProcessId processId) {
        this.processId = processId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PeppolMessageHeader");
        sb.append("{messageId=").append(messageId);
        sb.append(", channelId=").append(channelId);
        sb.append(", recipientId=").append(recipientId);
        sb.append(", senderId=").append(senderId);
        sb.append(", documentId=").append(documentId);
        sb.append(", processId=").append(processId);
        sb.append('}');
        return sb.toString();
    }
}
