package eu.peppol;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.TransmissionId;

import java.util.Date;

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
    String messageId;
    ParticipantId recipientId;
    ParticipantId senderId;
    String documentTypeIdentifier;
    String profileTypeIdentifier;
    String sendingAccessPoint;
    String receivingAccessPoint;
    BusDoxProtocol protocol = BusDoxProtocol.AS2;
    String userAgent = "oxalis";
    String userAgentVersion;
    Date   sendersTimeStamp;
    Date   receivedTimeStamp;
    String sendingAccessPointDistinguishedName;

    private TransmissionId transmissionId;

    public String getMessageId() {
        return messageId;
    }

    /** Unique message identifier, which is held in the SBDH of an AS2 Message.
     * Do not confuse with the AS2 Message-ID which is supplied as headers in the HTTP protocol. */
    public void setMessageId(String messageId) {
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

    public String getDocumentTypeIdentifier() {
        return documentTypeIdentifier;
    }

    public void setDocumentTypeIdentifier(String documentTypeIdentifier) {
        this.documentTypeIdentifier = documentTypeIdentifier;
    }

    public String getProfileTypeIdentifier() {
        return profileTypeIdentifier;
    }

    public void setProfileTypeIdentifier(String profileTypeIdentifier) {
        this.profileTypeIdentifier = profileTypeIdentifier;
    }

    public String getSendingAccessPoint() {
        return sendingAccessPoint;
    }

    public void setSendingAccessPoint(String sendingAccessPoint) {
        this.sendingAccessPoint = sendingAccessPoint;
    }

    public String getReceivingAccessPoint() {
        return receivingAccessPoint;
    }

    public void setReceivingAccessPoint(String receivingAccessPoint) {
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

    public String getSendingAccessPointDistinguishedName() {
        return sendingAccessPointDistinguishedName;
    }

    public void setSendingAccessPointDistinguishedName(String sendingAccessPointDistinguishedName) {
        this.sendingAccessPointDistinguishedName = sendingAccessPointDistinguishedName;
    }

    /** Holds the transmission identifier. I.e. the unique identifier assigned every time a transmission is executed.
     *
     * Do not confuse this with the Message identifier, which can be found in the SBDH.
     * @param transmissionId the unique identification of the transmission.
     */
    public void setTransmissionId(TransmissionId transmissionId) {
        this.transmissionId = transmissionId;
    }

    public TransmissionId getTransmissionId() {
        return transmissionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PeppolMessageMetaData{");
        sb.append("messageId='").append(messageId).append('\'');
        sb.append(", recipientId=").append(recipientId);
        sb.append(", senderId=").append(senderId);
        sb.append(", documentTypeIdentifier='").append(documentTypeIdentifier).append('\'');
        sb.append(", profileTypeIdentifier='").append(profileTypeIdentifier).append('\'');
        sb.append(", sendingAccessPoint='").append(sendingAccessPoint).append('\'');
        sb.append(", receivingAccessPoint='").append(receivingAccessPoint).append('\'');
        sb.append(", protocol=").append(protocol);
        sb.append(", userAgent='").append(userAgent).append('\'');
        sb.append(", userAgentVersion='").append(userAgentVersion).append('\'');
        sb.append(", sendersTimeStamp=").append(sendersTimeStamp);
        sb.append(", receivedTimeStamp=").append(receivedTimeStamp);
        sb.append(", sendingAccessPointDistinguishedName='").append(sendingAccessPointDistinguishedName).append('\'');
        sb.append(", transmissionId='").append(transmissionId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
