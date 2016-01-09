package eu.peppol;

import eu.peppol.identifier.*;

import java.io.Serializable;
import java.security.Principal;
import java.util.Date;

/**
 * Holds meta data obtained during transmission of a PEPPOL message.
 *
 * @author steinar
 *         Date: 24.10.13
 *         Time: 11:38
 * @since AS2 was introduced
 */
public class PeppolMessageMetaData implements Serializable {

    private static final long serialVersionUID = -7534628264798427902L;

    /**
     * The PEPPOL Message Identifier, supplied in the SBDH when using AS2
     */
    private MessageId messageId;

    /**
     * The PEPPOL Participant Identifier of the end point of the receiver
     */
    private ParticipantId recipientId;

    private ParticipantId senderId;
    private PeppolDocumentTypeId documentTypeIdentifier;
    private PeppolProcessTypeId profileTypeIdentifier;

    private AccessPointIdentifier sendingAccessPoint;
    private AccessPointIdentifier receivingAccessPoint;

    private BusDoxProtocol protocol = BusDoxProtocol.AS2;
    private String userAgent = null;
    private String userAgentVersion = null;
    private Date sendersTimeStamp;
    private Date receivedTimeStamp = new Date();

    private Principal sendingAccessPointPrincipal;

    private TransmissionId transmissionId;

    /**
     * Unique message identifier, which is held in the SBDH of an AS2 Message.
     * Do not confuse with the AS2 Message-ID which is supplied as headers in the HTTP protocol.
     */
    public MessageId getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = new MessageId(messageId);
    }

    public void setMessageId(MessageId messageId) {
        this.messageId = messageId;
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

    public PeppolProcessTypeId getProfileTypeIdentifier() {
        return profileTypeIdentifier;
    }

    public void setProfileTypeIdentifier(PeppolProcessTypeId profileTypeIdentifier) {
        this.profileTypeIdentifier = profileTypeIdentifier;
    }

    public AccessPointIdentifier getSendingAccessPoint() {
        return sendingAccessPoint;
    }

    public void setSendingAccessPointId(AccessPointIdentifier sendingAccessPoint) {
        this.sendingAccessPoint = sendingAccessPoint;
    }

    public AccessPointIdentifier getReceivingAccessPoint() {
        return receivingAccessPoint;
    }

    public void setReceivingAccessPoint(AccessPointIdentifier receivingAccessPoint) {
        this.receivingAccessPoint = receivingAccessPoint;
    }

    public BusDoxProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(BusDoxProtocol protocol) {
        this.protocol = protocol;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUserAgentVersion() {
        return userAgentVersion;
    }

    public void setUserAgentVersion(String userAgentVersion) {
        this.userAgentVersion = userAgentVersion;
    }

    public Date getSendersTimeStamp() {
        return sendersTimeStamp;
    }

    public void setSendersTimeStamp(Date sendersTimeStamp) {
        this.sendersTimeStamp = sendersTimeStamp;
    }

    public Date getReceivedTimeStamp() {
        return receivedTimeStamp;
    }

    public void setReceivedTimeStamp(Date receivedTimeStamp) {
        this.receivedTimeStamp = receivedTimeStamp;
    }


    /**
     * Holds the AS2 Message-ID, which is located in the HTTP Header
     *
     * @param transmissionId
     */
    public void setTransmissionId(TransmissionId transmissionId) {
        this.transmissionId = transmissionId;
    }

    public TransmissionId getTransmissionId() {
        return transmissionId;
    }

    public void setSendingAccessPoint(AccessPointIdentifier sendingAccessPoint) {
        this.sendingAccessPoint = sendingAccessPoint;
    }

    public Principal getSendingAccessPointPrincipal() {
        return sendingAccessPointPrincipal;
    }

    public void setSendingAccessPointPrincipal(Principal sendingAccessPointPrincipal) {
        this.sendingAccessPointPrincipal = sendingAccessPointPrincipal;
    }

    @Override
    public String toString() {
        return new StringBuilder("PeppolMessageMetaData{")
                .append("messageId=").append(messageId)
                .append(", recipientId=").append(recipientId)
                .append(", senderId=").append(senderId)
                .append(", documentTypeIdentifier=").append(documentTypeIdentifier)
                .append(", profileTypeIdentifier=").append(profileTypeIdentifier)
                .append(", sendingAccessPoint=").append(sendingAccessPoint)
                .append(", receivingAccessPoint=").append(receivingAccessPoint)
                .append(", protocol=").append(protocol)
                .append(", userAgent='").append(userAgent).append('\'')
                .append(", userAgentVersion='").append(userAgentVersion).append('\'')
                .append(", sendersTimeStamp=").append(sendersTimeStamp)
                .append(", receivedTimeStamp=").append(receivedTimeStamp)
                .append(", sendingAccessPointPrincipal=").append(sendingAccessPointPrincipal)
                .append(", transmissionId='").append(transmissionId).append('\'')
                .append('}')
                .toString();
    }
}
