package eu.peppol.as2.lang;

import eu.peppol.lang.OxalisException;

public class OxalisAs2Exception extends OxalisException {

    public OxalisAs2Exception(String message) {
        super(message);
    }

    public OxalisAs2Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
