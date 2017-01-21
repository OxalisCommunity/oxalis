package no.difi.oxalis.api.transmission;

import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProtocol;

import java.util.Date;

public interface TransmissionResult {

    Header getHeader();

    Date getTimestamp();

    TransportProtocol getTransportProtocol();
}
