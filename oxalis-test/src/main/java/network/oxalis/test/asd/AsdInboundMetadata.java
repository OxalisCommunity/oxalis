package network.oxalis.test.asd;

import network.oxalis.api.inbound.InboundMetadata;
import network.oxalis.api.model.TransmissionIdentifier;
import network.oxalis.api.tag.Tag;
import network.oxalis.vefa.peppol.common.model.*;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author erlend
 */
public class AsdInboundMetadata implements InboundMetadata {

    private TransmissionIdentifier transmissionIdentifier;

    private Header header;

    private Date timestamp;

    public AsdInboundMetadata(TransmissionIdentifier transmissionIdentifier, Header header, Date timestamp) {
        this.transmissionIdentifier = transmissionIdentifier;
        this.header = header;
        this.timestamp = timestamp;
    }

    @Override
    public X509Certificate getCertificate() {
        return null;
    }

    @Override
    public Tag getTag() {
        return null;
    }

    @Override
    public TransmissionIdentifier getTransmissionIdentifier() {
        return transmissionIdentifier;
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
    public Digest getDigest() {
        return null;
    }

    @Override
    public TransportProtocol getTransportProtocol() {
        return AsdConstants.TRANSPORT_PROTOCOL;
    }

    @Override
    public TransportProfile getProtocol() {
        return AsdConstants.TRANSPORT_PROFILE;
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
