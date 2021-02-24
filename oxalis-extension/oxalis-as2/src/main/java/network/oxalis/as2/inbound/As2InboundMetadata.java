/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.as2.inbound;

import network.oxalis.api.inbound.InboundMetadata;
import network.oxalis.api.model.TransmissionIdentifier;
import network.oxalis.api.tag.Tag;
import network.oxalis.api.timestamp.Timestamp;
import network.oxalis.vefa.peppol.common.model.*;

import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @author erlend
 */
class As2InboundMetadata implements InboundMetadata {

    private final TransmissionIdentifier transmissionIdentifier;

    private final Header header;

    private final Date timestamp;

    private final TransportProfile transportProfile;

    private final Digest digest;

    private final Receipt primaryReceipt;

    private final List<Receipt> receipts;

    private final X509Certificate certificate;

    private final Tag tag;

    public As2InboundMetadata(TransmissionIdentifier transmissionIdentifier, Header header, Timestamp timestamp,
                              TransportProfile transportProfile, Digest digest, X509Certificate certificate,
                              byte[] primaryReceipt, Tag tag) {
        this.transmissionIdentifier = transmissionIdentifier;
        this.header = header;
        this.timestamp = timestamp.getDate();
        this.transportProfile = transportProfile;
        this.digest = digest;
        this.certificate = certificate;
        this.primaryReceipt = Receipt.of("message/disposition-notification", primaryReceipt);
        this.tag = tag;

        List<Receipt> receipts = new ArrayList<>();
        receipts.add(this.primaryReceipt);
        if (timestamp.getReceipt().isPresent())
            receipts.add(timestamp.getReceipt().get());
        this.receipts = Collections.unmodifiableList(receipts);
    }

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
        return receipts;
    }

    @Override
    public Receipt primaryReceipt() {
        return primaryReceipt;
    }

    @Override
    public X509Certificate getCertificate() {
        return certificate;
    }

    @Override
    public Tag getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        As2InboundMetadata that = (As2InboundMetadata) o;
        return Objects.equals(transmissionIdentifier, that.transmissionIdentifier) &&
                Objects.equals(header, that.header) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(transportProfile, that.transportProfile) &&
                Objects.equals(digest, that.digest) &&
                Objects.equals(primaryReceipt, that.primaryReceipt) &&
                Objects.equals(receipts, that.receipts) &&
                Objects.equals(certificate, that.certificate) &&
                Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transmissionIdentifier, header, timestamp, transportProfile,
                digest, primaryReceipt, receipts, certificate, tag);
    }
}
