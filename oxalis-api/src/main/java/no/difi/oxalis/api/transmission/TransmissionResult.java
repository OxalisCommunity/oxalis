package no.difi.oxalis.api.transmission;

import no.difi.vefa.peppol.common.model.Digest;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;
import no.difi.vefa.peppol.common.model.TransportProtocol;

import java.util.Date;

public interface TransmissionResult {

    Header getHeader();

    Date getTimestamp();

    Digest getDigest();

    TransportProtocol getTransportProtocol();

    /**
     * The protocol used for the transmission
     */
    TransportProfile getProtocol();

}
