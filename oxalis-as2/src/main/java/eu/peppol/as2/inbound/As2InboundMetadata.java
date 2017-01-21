package eu.peppol.as2.inbound;

import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.vefa.peppol.common.model.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;

class As2InboundMetadata implements InboundMetadata {

    private final MessageId messageId;

    private final Header header;

    private final Date timestamp;

    private final TransportProfile transportProfile;

    private final Digest digest;

    public As2InboundMetadata(MessageId messageId, Header header, Timestamp timestamp,
                              TransportProfile transportProfile, Digest digest) {
        this.messageId = messageId;
        this.header = header;
        this.timestamp = timestamp.getDate();
        this.transportProfile = transportProfile;
        this.digest = digest;
    }

    @Override
    public MessageId getMessageId() {
        return messageId;
    }

    @Override
    public Header getHeader() {
        return header;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public TransportProfile getProtocol() {
        return transportProfile;
    }

    @Override
    public TransportProtocol getTransportProtocol() {
        return TransportProtocol.AS2;
    }

    @Override
    public Digest getDigest() {
        return digest;
    }

    @Override
    public List<Receipt> getReceipts() {
        return Collections.emptyList();
    }

    @Override
    public Receipt primaryReceipt() {
        return null;
    }
}
