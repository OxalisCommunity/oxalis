package eu.peppol.outbound.lang;

import eu.peppol.lang.OxalisException;

public class OxalisOutboundException extends OxalisException {

    public OxalisOutboundException(String message) {
        super(message);
    }

    public OxalisOutboundException(String message, Throwable cause) {
        super(message, cause);
    }
}
