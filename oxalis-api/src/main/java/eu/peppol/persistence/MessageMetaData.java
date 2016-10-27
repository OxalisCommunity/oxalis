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

import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

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
 *
 * @author steinar
 *         Date: 22.10.2016
 *         Time: 17.51
 */
public class MessageMetaData {

    MessageNumber messageNumber = null;
    Optional<AccessPointAccountId> accessPointAccountId = Optional.empty();
    TransferDirection transferDirection;
    LocalDateTime received = LocalDateTime.now();
    Optional<LocalDateTime> delivered = Optional.empty();
    ParticipantId sender;
    ParticipantId receiver;
    ChannelProtocol channelProtocol;
    MessageId messageId  ;

    PeppolDocumentTypeId documentTypeId;
    Optional<PeppolProcessTypeId> processTypeId = Optional.empty();
    Optional<AccessPointIdentifier> accessPointIdentifier = Optional.empty();
    Optional<Principal> principal = Optional.empty();
    URI payloadUri;
    Optional<URI> genericEvidenceUri = Optional.empty();
    Optional<URI> nativeEvidenceUri = Optional.empty();

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

    public Optional<AccessPointAccountId> getAccessPointAccountId() {
        return accessPointAccountId;
    }

    public TransferDirection getTransferDirection() {
        return transferDirection;
    }

    public LocalDateTime getReceived() {
        return received;
    }

    public Optional<LocalDateTime> getDelivered() {
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

    public Optional<PeppolProcessTypeId> getProcessTypeId() {
        return processTypeId;
    }

    public Optional<AccessPointIdentifier> getAccessPointIdentifier() {
        return accessPointIdentifier;
    }

    public Optional<Principal> getPrincipal() {
        return principal;
    }

    public URI getPayloadUri() {
        return payloadUri;
    }

    public Optional<URI> getGenericEvidenceUri() {
        return genericEvidenceUri;
    }

    public Optional<URI> getNativeEvidenceUri() {
        return nativeEvidenceUri;
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
        private Optional<LocalDateTime> delivered = Optional.empty();
        private MessageNumber messageNumber;
        private Optional<AccessPointAccountId> accessPointAccountId = Optional.empty();
        private LocalDateTime received = LocalDateTime.now();
        private Optional<PeppolProcessTypeId> processTypeId = Optional.empty();
        private Optional<AccessPointIdentifier> accessPointIdentifier = Optional.empty();
        private Optional<Principal> principal = Optional.empty();
        private URI payloadUri;
        private Optional<URI> genericEvidenceUri = Optional.empty();
        private Optional<URI> nativeEvidenceUri = Optional.empty();


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
            this.accessPointAccountId = Optional.of(new AccessPointAccountId(i));
            return this;
        }

        public Builder received(LocalDateTime dateTime) {
            this.received = dateTime;
            return this;
        }

        public Builder delivered(LocalDateTime delivered) {
            this.delivered = Optional.ofNullable(delivered);
            return this;
        }

        public Builder messageId(MessageId messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder processTypeId(PeppolProcessTypeId peppolProcessTypeId) {
            this.processTypeId = Optional.ofNullable(peppolProcessTypeId);
            return this;
        }

        public Builder accessPointIdentifier(AccessPointIdentifier accessPointIdentifier) {
            this.accessPointIdentifier = Optional.ofNullable(accessPointIdentifier);
            return this;
        }

        public Builder apPrincipal(Principal principal) {
            this.principal = Optional.ofNullable(principal);
            return this;
        }

        public Builder payloadUri(URI payloadUri) {
            this.payloadUri = payloadUri;
            return this;
        }

        public Builder genericEvidenceUri(URI genericEvidenceUri) {
            this.genericEvidenceUri = Optional.ofNullable(genericEvidenceUri);
            return this;
        }

        public Builder nativeEvidenceUri(URI nativeEvidenceUri) {
            this.nativeEvidenceUri = Optional.ofNullable(nativeEvidenceUri);
            return this;
        }
    }   // end of Builder
}
