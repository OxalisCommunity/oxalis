package no.difi.oxalis.api.transmission;

import eu.peppol.identifier.MessageId;
import no.difi.vefa.peppol.common.model.*;

import java.util.Date;
import java.util.List;

public interface TransmissionResult {

    /**
     * Transmission id assigned during transmission
     */
    MessageId getMessageId();

    Header getHeader();

    Date getTimestamp();

    Digest getDigest();

    TransportProtocol getTransportProtocol();

    /**
     * The protocol used for the transmission
     */
    TransportProfile getProtocol();

    List<Receipt> getReceipts();

    Receipt primaryReceipt();

}
