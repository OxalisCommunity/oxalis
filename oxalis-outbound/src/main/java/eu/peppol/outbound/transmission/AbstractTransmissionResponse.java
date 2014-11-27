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
public class AbstractTransmissionResponse implements TransmissionResponse {

    TransmissionId transmissionId;
    private final PeppolStandardBusinessHeader sbdh;
    private final URL url;
    private final BusDoxProtocol busDoxProtocol;
    private final CommonName commonName;

    public AbstractTransmissionResponse(TransmissionId transmissionId, PeppolStandardBusinessHeader sbdh, URL url, BusDoxProtocol busDoxProtocol, CommonName commonName) {
        this.transmissionId = transmissionId;
        this.sbdh = sbdh;
        this.url = url;
        this.busDoxProtocol = busDoxProtocol;
        this.commonName = commonName;
    }

    @SuppressWarnings("unused")
    public PeppolStandardBusinessHeader getStandardBusinessHeader() {
        return sbdh;
    }

    @Override
    public TransmissionId getTransmissionId() {
        return transmissionId;
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public BusDoxProtocol getProtocol() {
        return busDoxProtocol;
    }

    @Override
    public CommonName getCommonName() {
        return commonName;
    }

}
