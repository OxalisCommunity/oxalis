package eu.peppol;

import eu.peppol.identifier.*;

import java.util.Date;

/**
 * @author steinar
 *         Date: 04.11.13
 *         Time: 13:44
 */
public class PeppolStandardBusinessHeader {

    private ParticipantId recipientId;
    private ParticipantId senderId;
    private PeppolDocumentTypeId peppolDocumentTypeId;
    private PeppolProcessTypeId profileTypeIdentifier;

    /** Represents the unique identity of a message, which is assigned upon creation of the envelope (SBDH).
     *
     * This messageId does not correspond to the "messageId", which should have been named "transmission id",
     * which is found in the START header.
     *
     * <code>//StandardBusinessDocumentHeader/DocumentIdentification/InstanceIdentifier</code>
     */
    private MessageId messageId;
    private Date creationDateAndTime;


    public void setRecipientId(ParticipantId recipientId) {
        this.recipientId = recipientId;
    }

    public ParticipantId getRecipientId() {
        return recipientId;
    }

    public void setSenderId(ParticipantId senderId) {
        this.senderId = senderId;
    }

    public ParticipantId getSenderId() {
        return senderId;
    }

    public void setMessageId(MessageId messageId) {
        this.messageId = messageId;
    }

    public MessageId getMessageId() {
        return messageId;
    }

    public void setCreationDateAndTime(Date creationDateAndTime) {
        this.creationDateAndTime = creationDateAndTime;
    }

    public Date getCreationDateAndTime() {
        return creationDateAndTime;
    }

    public void setDocumentTypeIdentifier(PeppolDocumentTypeId documentTypeIdentifier) {
        this.peppolDocumentTypeId = documentTypeIdentifier;
    }

    public PeppolDocumentTypeId getDocumentTypeIdentifier() {
        return peppolDocumentTypeId;
    }

    public void setProfileTypeIdentifier(PeppolProcessTypeId profileTypeIdentifier) {
        this.profileTypeIdentifier = profileTypeIdentifier;
    }

    public PeppolProcessTypeId getProfileTypeIdentifier() {
        return profileTypeIdentifier;
    }
}
