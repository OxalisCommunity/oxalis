package no.difi.oxalis.api.lang;

import eu.peppol.lang.OxalisException;

public class TimestampException extends OxalisException {

    public TimestampException(String message) {
        super(message);
    }

    public TimestampException(String message, Throwable cause) {
        super(message, cause);
    }
}
