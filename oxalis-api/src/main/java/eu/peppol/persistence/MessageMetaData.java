/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.persistence;

import eu.peppol.identifier.*;

import java.io.Serializable;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;

/**
 * Reflects the message meta data as persisted in the SQL database.
 * <p>
 * The use of {@link eu.peppol.PeppolMessageMetaData} in Oxalis and the various other variants in "Ringo" is a mess and needs to be consolidated and cleaned up
 * <p>
 * A message is either "inbound" or "outbound" relative to the PEPPOL network, i.e. a message can have four states:
 * <ol>
 * <li>Received, Inbound - messge received from the PEPPOL network</li>
 * <li>Received, Outbound - message received from the back-end destined for transmission in the PEPPOL network</li>
 * <li>Sent (delivered), Inbound - message, previously received from the PEPPOL network, was sent to the back-end</li>
 * <li>Sent (delivered), Outbound - message, previously received from the back-end, was sent to through the PEPPOL network</li>
 * </ol>
 * <p>
 *     TODO: Split into separate objects for transmission and reception
 * </p>
 *
 * @author steinar
 *         Date: 22.10.2016
 *         Time: 17.51
 */
public class MessageMetaData implements Serializable {

    MessageNumber messageNumber;
    AccountId accessPointAccountId;
    TransferDirection transferDirection;
    LocalDateTime received = LocalDateTime.now();
    LocalDateTime delivered;
    ParticipantId sender;
    ParticipantId receiver;
    ChannelProtocol channelProtocol;
    MessageId messageId  ;

    PeppolDocumentTypeId documentTypeId;
    PeppolProcessTypeId processTypeId ;
    AccessPointIdentifier accessPointIdentifier;
    Principal principal;
    URI payloadUri;
    URI genericEvidenceUri;
    URI nativeEvidenceUri ;

    public void setAccountId(AccountId accessPointAccountId) {
        this.accessPointAccountId = accessPointAccountId;
    }

    public MessageMetaData(Builder builder) {

        messageNumber = builder.messageNumber;
        accessPointAccountId = builder.accessPointAccountId;
        transferDirection = builder.transferDirection;
        received = builder.received;
        delivered = builder.delivered;
        sender = builder.sender;
        receiver = builder.receiver;
        channelProtocol = builder.channelProtocol;
        messageId = builder.messageId;
        documentTypeId = builder.documentTypeId;
        processTypeId = builder.processTypeId;
        accessPointIdentifier = builder.accessPointIdentifier;
        principal = builder.principal;
        payloadUri = builder.payloadUri;
        genericEvidenceUri = builder.genericEvidenceUri;
        nativeEvidenceUri = builder.nativeEvidenceUri;

    }

    public MessageNumber getMessageNumber() {
        return messageNumber;
    }

    public AccountId getAccountId() {
        return accessPointAccountId;
    }

    public TransferDirection getTransferDirection() {
        return transferDirection;
    }

    public LocalDateTime getReceived() {
        return received;
    }

    public LocalDateTime getDelivered() {
        return delivered;
    }

    public ParticipantId getSender() {
        return sender;
    }

    public ParticipantId getReceiver() {
        return receiver;
    }

    public ChannelProtocol getChannelProtocol() {
        return channelProtocol;
    }

    public MessageId getMessageId() {
        return messageId;
    }

    public PeppolDocumentTypeId getDocumentTypeId() {
        return documentTypeId;
    }

    public PeppolProcessTypeId getProcessTypeId() {
        return processTypeId;
    }

    public AccessPointIdentifier getAccessPointIdentifier() {
        return accessPointIdentifier;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public URI getPayloadUri() {
        return payloadUri;
    }

    public URI getGenericEvidenceUri() {
        return genericEvidenceUri;
    }

    public URI getNativeEvidenceUri() {
        return nativeEvidenceUri;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageMetaData{");
        sb.append("messageNumber=").append(messageNumber);
        sb.append(", accessPointAccountId=").append(accessPointAccountId);
        sb.append(", transferDirection=").append(transferDirection);
        sb.append(", received=").append(received);
        sb.append(", delivered=").append(delivered);
        sb.append(", sender=").append(sender);
        sb.append(", receiver=").append(receiver);
        sb.append(", channelProtocol=").append(channelProtocol);
        sb.append(", messageId=").append(messageId);
        sb.append(", documentTypeId=").append(documentTypeId);
        sb.append(", processTypeId=").append(processTypeId);
        sb.append(", accessPointIdentifier=").append(accessPointIdentifier);
        sb.append(", principal=").append(principal);
        sb.append(", payloadUri=").append(payloadUri);
        sb.append(", genericEvidenceUri=").append(genericEvidenceUri);
        sb.append(", nativeEvidenceUri=").append(nativeEvidenceUri);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        // Required
        private final TransferDirection transferDirection;
        private final ParticipantId sender;
        private final ParticipantId receiver;
        private final PeppolDocumentTypeId documentTypeId;
        private final ChannelProtocol channelProtocol;
        private MessageId messageId = new MessageId();

        // Optional
        private LocalDateTime delivered ;
        private MessageNumber messageNumber;
        private AccountId accessPointAccountId ;
        private LocalDateTime received = LocalDateTime.now();
        private PeppolProcessTypeId processTypeId;
        private AccessPointIdentifier accessPointIdentifier;
        private Principal principal;
        private URI payloadUri;
        private URI genericEvidenceUri;
        private URI nativeEvidenceUri;


        public Builder(TransferDirection transferDirection, ParticipantId sender, ParticipantId receiver, PeppolDocumentTypeId documentTypeId, ChannelProtocol channelProtocol) {

            this.transferDirection = transferDirection;
            this.sender = sender;
            this.receiver = receiver;
            this.documentTypeId = documentTypeId;
            this.channelProtocol = channelProtocol;

            if (transferDirection == null) {
                throw new IllegalArgumentException("TransferDirection is required");
            }
            if (sender == null) {
                throw new IllegalArgumentException("Sender is required");
            }
            if (receiver == null) {
                throw new IllegalArgumentException("Receiver is required");
            }
            if (documentTypeId == null) {
                throw new IllegalArgumentException("Document type Id is required");
            }
            if (channelProtocol == null) {
                throw new IllegalArgumentException("Channel protocol is required");
            }
            if (received == null) {
                throw new IllegalArgumentException("Received dateTime is required");
            }
        }

        public MessageMetaData build() {
            MessageMetaData m = new MessageMetaData(this);


            if (m.messageId == null) {
                throw new IllegalStateException("Inbound messages should always have a transmission Id assigned by the sender");
                // The message has been sent into the PEPPOL network
            }
            return m;
        }


        public Builder messageNumber(Long l) {
            this.messageNumber = new MessageNumber(l);
            return this;
        }

        public Builder accountId(Integer i) {
            this.accessPointAccountId = new AccountId(i);
            return this;
        }

        public Builder received(LocalDateTime dateTime) {
            this.received = dateTime;
            return this;
        }

        public Builder delivered(LocalDateTime delivered) {
            this.delivered = delivered;
            return this;
        }

        public Builder messageId(MessageId messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder processTypeId(PeppolProcessTypeId peppolProcessTypeId) {
            this.processTypeId = peppolProcessTypeId;
            return this;
        }

        public Builder accessPointIdentifier(AccessPointIdentifier accessPointIdentifier) {
            this.accessPointIdentifier = accessPointIdentifier;
            return this;
        }

        public Builder apPrincipal(Principal principal) {
            this.principal = principal;
            return this;
        }

        public Builder payloadUri(URI payloadUri) {
            this.payloadUri = payloadUri;
            return this;
        }

        public Builder genericEvidenceUri(URI genericEvidenceUri) {
            this.genericEvidenceUri = genericEvidenceUri;
            return this;
        }

        public Builder nativeEvidenceUri(URI nativeEvidenceUri) {
            this.nativeEvidenceUri = nativeEvidenceUri;
            return this;
        }

        public Builder accountId(AccountId id) {
            this.accessPointAccountId = id;
            return this;
        }
    }   // end of Builder
}
