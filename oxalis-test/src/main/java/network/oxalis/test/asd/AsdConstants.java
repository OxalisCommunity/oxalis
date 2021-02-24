package network.oxalis.test.asd;

import network.oxalis.vefa.peppol.common.lang.PeppolException;
import network.oxalis.vefa.peppol.common.model.TransportProfile;
import network.oxalis.vefa.peppol.common.model.TransportProtocol;

/**
 * @author erlend
 */
public class AsdConstants {

    public static final TransportProtocol TRANSPORT_PROTOCOL;

    public static final TransportProfile TRANSPORT_PROFILE = TransportProfile.of("bdx-transport-asd");

    static {
        try {
            TRANSPORT_PROTOCOL = TransportProtocol.of("ASD");
        } catch (PeppolException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
