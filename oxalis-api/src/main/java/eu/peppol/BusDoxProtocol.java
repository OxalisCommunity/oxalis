package eu.peppol;

/**
 * Finite set of known BusDox transport protocols.
 *
 * @author steinar
 *         Date: 24.10.13
 *         Time: 11:45
 */
public enum BusDoxProtocol {

    START("busdox-transport-start"),
    AS2("busdox-transport-as2-100");

    private final String protocolName;

    BusDoxProtocol(String protocolName) {
        this.protocolName = protocolName;
    }

    public static BusDoxProtocol instanceFrom(String transportProfile) {
        for (BusDoxProtocol busDoxProtocol : values()) {
            if (busDoxProtocol.protocolName.equalsIgnoreCase(transportProfile) || busDoxProtocol.name().equalsIgnoreCase(transportProfile)) {
                return busDoxProtocol;
            }
        }

        throw new IllegalStateException("Unknown protocol name : " + transportProfile);
    }
}
