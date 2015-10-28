package eu.peppol.lang;

/**
 * Base exception of the Oxalis exception hierarchy. Thrown exceptions must use a subclass of this to indicate type
 * of exception for better handling.
 */
public abstract class OxalisException extends Exception {
    public OxalisException(String message) {
        super(message);
    }

    public OxalisException(String message, Throwable cause) {
        super(message, cause);
    }
}
