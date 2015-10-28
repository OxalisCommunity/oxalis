package eu.peppol.lang;

/**
 * Security exceptions are always thrown to indicate a certain action would involve stepping outside
 * current security domain, and forcing such action must be seen as a no-go.
 */
public class OxalisSecurityException extends OxalisException {
    public OxalisSecurityException(String message) {
        super(message);
    }

    public OxalisSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
