package eu.peppol.start.identifier;

/**
 * Holds the PEPPOL headers supplied in the SOAP request.
 *
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
    PeppolDocumentTypeId documentTypeIdentifier;
    PeppolProcessTypeId peppolProcessTypeId;

    // This is not part of the specification, but it is very useful
    String remoteHost;

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

    public PeppolDocumentTypeId getDocumentTypeIdentifier() {
        return documentTypeIdentifier;
    }

    public void setDocumentTypeIdentifier(PeppolDocumentTypeId documentTypeIdentifier) {
        this.documentTypeIdentifier = documentTypeIdentifier;
    }

    public PeppolProcessTypeId getPeppolProcessTypeId() {
        return peppolProcessTypeId;
    }

    public void setPeppolProcessTypeId(PeppolProcessTypeId peppolProcessTypeId) {
        this.peppolProcessTypeId = peppolProcessTypeId;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public String toString() {
        return "PeppolMessageHeader{" +
                "messageId=" + messageId +
                ", channelId=" + channelId +
                ", recipientId=" + recipientId +
                ", senderId=" + senderId +
                ", documentTypeIdentifier=" + documentTypeIdentifier +
                ", peppolProcessTypeId=" + peppolProcessTypeId +
                ", remoteHost='" + remoteHost + '\'' +
                '}';
    }
}
