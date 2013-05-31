package eu.peppol.start.identifier;

import java.security.Principal;

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

    // Also not part of specification, but even more useful
    Principal remoteAccessPointPrincipal;

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

    public Principal getRemoteAccessPointPrincipal() {
        return remoteAccessPointPrincipal;
    }

    public void setRemoteAccessPointPrincipal(Principal remoteAccessPointPrincipal) {
        this.remoteAccessPointPrincipal = remoteAccessPointPrincipal;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PeppolMessageHeader{");
        sb.append("messageId=").append(messageId);
        sb.append(", channelId=").append(channelId);
        sb.append(", recipientId=").append(recipientId);
        sb.append(", senderId=").append(senderId);
        sb.append(", documentTypeIdentifier=").append(documentTypeIdentifier);
        sb.append(", peppolProcessTypeId=").append(peppolProcessTypeId);
        sb.append(", remoteHost='").append(remoteHost).append('\'');
        sb.append(", remoteAccessPointPrincipal=").append(remoteAccessPointPrincipal != null ? remoteAccessPointPrincipal.getName() : null);
        sb.append('}');
        return sb.toString();
    }
}
