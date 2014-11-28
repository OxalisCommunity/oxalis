package eu.peppol.outbound.transmission;

import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.security.CommonName;

import java.net.URL;

/**
 * @author steinar
 * @author thore
 */
public interface TransmissionResponse {

    /**
     * Transmission id assigned during transmission
     */
    TransmissionId getTransmissionId();

    /**
     * Get the effective SBDH used to decide transmission
     */
    public PeppolStandardBusinessHeader getStandardBusinessHeader();

    /**
     * The destination URL for the transmission
     */
    public URL getURL();

    /**
     * The protocol used for the transmission
     */
    public BusDoxProtocol getProtocol();

    /**
     * The common name of the receiver certificate
     */
    public CommonName getCommonName();

}
