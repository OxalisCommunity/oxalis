package eu.peppol;

import eu.peppol.identifier.*;

import java.security.Principal;
import java.util.Date;
import java.util.UUID;

/**
 * Holds meta data obtained during transmission of a PEPPOL message.
 *
 * @author steinar
 *         Date: 24.10.13
 *         Time: 11:38
 *
 * @since AS2 was introduced
 */
public class PeppolMessageMetaData {

    /** The PEPPOL Message Identifier, supplied in the SBDH when using AS2 */
    private MessageId messageId;

    /** The PEPPOL Participant Identifier of the end point of the receiver */
    private ParticipantId recipientId;

    private ParticipantId senderId;
    private PeppolDocumentTypeId documentTypeIdentifier;
    private PeppolProcessTypeId profileTypeIdentifier;

    private AccessPointIdentifier sendingAccessPoint;
    private AccessPointIdentifier receivingAccessPoint;

    private BusDoxProtocol protocol = BusDoxProtocol.AS2;
    private String userAgent = null;
    private String userAgentVersion = null;
    private Date   sendersTimeStamp;
    private Date   receivedTimeStamp = new Date();

    private Principal sendingAccessPointPrincipal;

    private TransmissionId transmissionId;

    /** Technical unique identifier of the message */
    private UUID id;

    /** Unique message identifier, which is held in the SBDH of an AS2 Message.
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


    /** Holds the AS2 Message-ID, which is located in the HTTP Header
     * @param transmissionId */
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PeppolMessageMetaData{");
        sb.append("messageId=").append(messageId);
        sb.append(", recipientId=").append(recipientId);
        sb.append(", senderId=").append(senderId);
        sb.append(", documentTypeIdentifier=").append(documentTypeIdentifier);
        sb.append(", profileTypeIdentifier=").append(profileTypeIdentifier);
        sb.append(", sendingAccessPoint=").append(sendingAccessPoint);
        sb.append(", receivingAccessPoint=").append(receivingAccessPoint);
        sb.append(", protocol=").append(protocol);
        sb.append(", userAgent='").append(userAgent).append('\'');
        sb.append(", userAgentVersion='").append(userAgentVersion).append('\'');
        sb.append(", sendersTimeStamp=").append(sendersTimeStamp);
        sb.append(", receivedTimeStamp=").append(receivedTimeStamp);
        sb.append(", sendingAccessPointPrincipal=").append(sendingAccessPointPrincipal);
        sb.append(", transmissionId='").append(transmissionId).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
