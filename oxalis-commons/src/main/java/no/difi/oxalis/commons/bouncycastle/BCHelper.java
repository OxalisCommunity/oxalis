package no.difi.oxalis.commons.bouncycastle;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

/**
 * Collection of functionality related to BouncyCastle.
 *
 * @author erlend
 * @since 4.0.0
 */
public class BCHelper {

    static {
        registerProvider();
    }

    /**
     * Registers BouncyCastle as provider if not already registered.
     */
    public static void registerProvider() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Creates a MessageDigest object using the BouncyCastle provider. Exception {@link NoSuchProviderException} is
     * disguised as {@link NoSuchAlgorithmException}.
     *
     * @param algorithm Algorithm to be use to create the MessageDigest object.
     * @return MessageDigest object ready for use.
     * @throws NoSuchAlgorithmException Thrown in cases when unknown algorithms are requestes.
     */
    public static MessageDigest getMessageDigest(String algorithm) throws NoSuchAlgorithmException {
        try {
            return MessageDigest.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(e.getMessage(), e);
        }
    }
}
