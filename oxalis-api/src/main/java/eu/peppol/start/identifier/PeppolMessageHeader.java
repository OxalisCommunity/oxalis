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
    DocumentTypeIdentifier documentTypeIdentifier;
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

    public DocumentTypeIdentifier getDocumentTypeIdentifier() {
        return documentTypeIdentifier;
    }

    public void setDocumentTypeIdentifier(DocumentTypeIdentifier documentTypeIdentifier) {
        this.documentTypeIdentifier = documentTypeIdentifier;
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
        sb.append(", documentTypeIdentifier=").append(documentTypeIdentifier);
        sb.append(", processId=").append(processId);
        sb.append('}');
        return sb.toString();
    }
}
