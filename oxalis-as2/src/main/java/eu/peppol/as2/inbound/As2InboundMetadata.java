package eu.peppol.as2.inbound;

import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.vefa.peppol.common.model.Digest;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;
import no.difi.vefa.peppol.common.model.TransportProtocol;

import java.util.Date;

class As2InboundMetadata implements InboundMetadata {

    private final Header header;

    private final Date timestamp;

    private final TransportProfile transportProfile;

    private final Digest digest;

    public As2InboundMetadata(Header header, Timestamp timestamp, TransportProfile transportProfile, Digest digest) {
        this.header = header;
        this.timestamp = timestamp.getDate();
        this.transportProfile = transportProfile;
        this.digest = digest;
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
}
