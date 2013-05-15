package eu.peppol.security;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

/**
 * @author steinar
 *         Date: 13.05.13
 *         Time: 12:56
 */
public class UnwrapSymmetricKeyException extends RuntimeException {
    public UnwrapSymmetricKeyException(String encodedSymmetricKey, GeneralSecurityException cause) {
        super("Unable to unwrap and decrypt wrapped symmetric key; " + cause.getMessage(), cause);
    }
}
