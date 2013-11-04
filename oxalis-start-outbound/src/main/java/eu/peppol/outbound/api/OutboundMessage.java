package eu.peppol.outbound.api;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Holds an outbound PEPPOL message.
 *
 * @author steinar
 *         Date: 31.10.13
 *         Time: 13:52
 */
public class OutboundMessage {

    private final ParticipantId receiver;
    private final ParticipantId sender;
    private final PeppolDocumentTypeId peppolDocumentTypeId;
    private final InputStream inputStream;
    private final PeppolProcessTypeId peppolProcessTypeid;

    private OutboundMessage(Builder builder) {
        receiver = builder.receiver;
        sender = builder.sender;
        peppolDocumentTypeId = builder.peppolDocumentTypeId;
        inputStream = builder.inputStream;
        peppolProcessTypeid = builder.peppolProcessTypeId;
    }

    public ParticipantId getReceiver() {
        return receiver;
    }

    public ParticipantId getSender() {
        return sender;
    }

    public PeppolDocumentTypeId getPeppolDocumentTypeId() {
        return peppolDocumentTypeId;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public PeppolProcessTypeId getPeppolProcessTypeid() {
        return peppolProcessTypeid;
    }

    public static class Builder {
        private PeppolDocumentTypeId peppolDocumentTypeId;
        private ParticipantId receiver;
        private ParticipantId sender;
        private InputStream inputStream;
        private PeppolProcessTypeId peppolProcessTypeId;

        public Builder() {
        }

        public Builder(PeppolDocumentTypeId peppolDocumentTypeId, ParticipantId receiver, ParticipantId sender, InputStream inputStream) {
            this.peppolDocumentTypeId = peppolDocumentTypeId;
            this.receiver = receiver;
            this.sender = sender;
            this.inputStream = inputStream;
        }

        public Builder requiredContentsFrom(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        public Builder requiredSender(ParticipantId sender) {

            this.sender = sender;
            return this;
        }

        public Builder requiredReceiver(ParticipantId receiver){

            this.receiver = receiver;
            return this;
        }

        public Builder requiredDocumentType(PeppolDocumentTypeId peppolDocumentTypeId) {

            this.peppolDocumentTypeId = peppolDocumentTypeId;
            return this;
        }

        public Builder optionalProcessType(PeppolProcessTypeId peppolProcessTypeId) {

            this.peppolProcessTypeId = peppolProcessTypeId;
            return this;
        }

        public OutboundMessage build() {
            requiredNotNull(receiver, "receiver");
            requiredNotNull(sender, "sender");
            requiredNotNull(inputStream, "contents");
            requiredNotNull(peppolDocumentTypeId, "documentType");
            return new OutboundMessage(this);
        }

        private void requiredNotNull(Object object, String name) {
            if (object == null) {
                throw new IllegalStateException("Required property '" + name + "' has not been set");
            }
        }
    }
}
