package no.difi.oxalis.api.lang;

import eu.peppol.lang.OxalisException;

/**
 * @author erlend
 * @since 4.0.0
 */
public class TimestampException extends OxalisException {

    public TimestampException(String message) {
        super(message);
    }

    public TimestampException(String message, Throwable cause) {
        super(message, cause);
    }
}
