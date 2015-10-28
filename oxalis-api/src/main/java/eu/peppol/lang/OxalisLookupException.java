package eu.peppol.lang;

/**
 * This exception indicates problems when performing lookup, especially SMP lookup. Forced sending after receiving
 * this exception should be seen as a no-go.
 */
public class OxalisLookupException extends OxalisException {
    public OxalisLookupException(String message) {
        super(message);
    }

    public OxalisLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
