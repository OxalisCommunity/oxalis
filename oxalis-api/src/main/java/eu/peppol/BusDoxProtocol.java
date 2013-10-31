package eu.peppol;

/**
 * @author steinar
 *         Date: 24.10.13
 *         Time: 11:45
 */
public enum BusDoxProtocol {

    START("busdox-transport-start"),AS2("busdox-transport-as2");

    private final String protocolName;

    BusDoxProtocol(String protocolName) {
        this.protocolName = protocolName;
    }

    public static BusDoxProtocol instanceFrom(String protocolName) {
        for (BusDoxProtocol busDoxProtocol : values()) {
            if (busDoxProtocol.protocolName.equalsIgnoreCase(protocolName)) {
                return busDoxProtocol;
            }
        }

        throw new IllegalStateException("Unknown protocol name : " + protocolName);
    }
}
