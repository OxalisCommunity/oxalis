package eu.peppol.as2.inbound;

import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProtocol;

import java.util.Date;

public class As2InboundMetadata implements InboundMetadata {

    private final Header header;

    private final Date timestamp;

    public As2InboundMetadata(Header header, Date timestamp) {
        this.header = header;
        this.timestamp = timestamp;
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
    public TransportProtocol getTransportProtocol() {
        return TransportProtocol.AS2;
    }
}
