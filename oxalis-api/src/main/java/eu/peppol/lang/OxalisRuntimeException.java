package eu.peppol.lang;

public abstract class OxalisRuntimeException extends RuntimeException {

    public OxalisRuntimeException(String message) {
        super(message);
    }

    public OxalisRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
