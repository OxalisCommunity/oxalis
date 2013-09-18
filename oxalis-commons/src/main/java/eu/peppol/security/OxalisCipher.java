package eu.peppol.security;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;

/**
 * Oxalis' cipher implementation. Encapsulates information required for encryption and decryption of data using
 * a symmetric key.
 *
 * @author steinar
 *         Date: 06.05.13
 *         Time: 21:22
 */
public class OxalisCipher {

    // Advanced Encryption Standard (AES) with Electronic Cookbook Mode (ECB) block mode and PKCS5 Padding
    // FIXME: Determine why specifying AES/ECB/PKCS5Padding fails when compiling with maven
    public static final String SYMMETRIC_KEY_ALGORITHM = "AES";

    /** Name of our encrypted (wrapped) symmetric key. Typically used in HTTP headers, name and value pairs, etc. */
    public static final String WRAPPED_SYMMETRIC_KEY_NAME = "PEPPOL-wrapped-key";

    public static final Logger log = LoggerFactory.getLogger(OxalisCipher.class);
    private SecretKey secretKey;

    public OxalisCipher() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(SYMMETRIC_KEY_ALGORITHM);
             secretKey = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to create symmetric key for " + SYMMETRIC_KEY_ALGORITHM + e, e);
        }
    }

    public OxalisCipher(SecretKey secretKey) {
        this.secretKey = secretKey;
    }


    /**
     * Wraps the supplied OutputStream in a encrypted cipher stream, i.e. every plain text byte written
     * into the new OutputStream is encrypted using our SecretKey.
     *
     * @param outputStream the plaint text output stream to encrypted
     * @return a new OutputStream which will encrypt every byte written to it.
     * @see #decryptStream(java.io.InputStream)
     */
    public OutputStream encryptStream(OutputStream outputStream) {
        Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);
        return new CipherOutputStream(outputStream, cipher);
    }

    /**
     * Wraps the supplied InputStream in a decrypted cipher stream, i.e. every encrypted byte read from
     * the new InputStream reads is decrypted using our SecretKey.
     *
     * @param inputStream the encrypted input stream
     * @return new InputStream which will decrypt every byte read from it.
     */
    public InputStream decryptStream(InputStream inputStream) {

        Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
        return new CipherInputStream(inputStream, cipher);
    }


    Cipher createCipher(int encryptMode) {
        if (secretKey == null) {
            throw new IllegalStateException("No symmetric secret key available");
        }

        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(SYMMETRIC_KEY_ALGORITHM);
            cipher.init(encryptMode, secretKey);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to create cipher for algorithm " + SYMMETRIC_KEY_ALGORITHM, e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException("Default padding does not work with algorithm " + SYMMETRIC_KEY_ALGORITHM, e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Invalid " + SYMMETRIC_KEY_ALGORITHM + " key");
        }
        return cipher;
    }


    public SecretKey getSecretKey() {
        return secretKey;
    }

    byte[] encrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);
        return cipher.doFinal(bytes);
    }

    byte[] decrypt(byte[] encryptedBytes) throws BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
        return cipher.doFinal(encryptedBytes);
    }
}
