package eu.peppol.security;

import org.testng.annotations.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.cert.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * User: steinar
 * Date: 13.12.12
 * Time: 15:43
 */
public class SignatureTest {


    private final String plainText = "The quick brown fox jumped over the lazy dog";

    @Test
    public void testSigning() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {


        KeyPair keyPair = generateKeyPair();

        Signature signature = Signature.getInstance("SHA1withDSA");

        PrivateKey privateKey = keyPair.getPrivate();

        signature.initSign(privateKey);

        signature.update(plainText.getBytes());

        byte[] signatureBytes = signature.sign();


        Signature signature2 = Signature.getInstance("SHA1withDSA");
        signature2.initVerify(keyPair.getPublic());
        signature2.update(plainText.getBytes());

        boolean  verifies = signature2.verify(signatureBytes);
        assertTrue(verifies);
    }


    @Test void testSaveAndReadPublicKey() throws NoSuchProviderException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
        KeyPair keyPair = generateKeyPair();

        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null);
        keyStore.setKeyEntry("dumbo", keyPair.getPrivate().getEncoded(), (java.security.cert.Certificate[]) null);
    }


    @Test(enabled = false)
    public void testEncryptionAndDecryption() throws NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        /**
         * FIXME: encryption with DSA keys does not work. Seems a better solution is to use encrypt/decrypt with AES and to use RSA or perhaps DSA for the encryption of the AES key
         *
         */
        KeyPair keyPair = generateKeyPair();

        byte[] encryptedBytes = encrypt(keyPair);

        String plain = decrypt(encryptedBytes, keyPair.getPrivate());

        assertEquals(plainText,plain);


    }

    private String decrypt(byte[] encryptedBytes, PrivateKey aPrivate) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.DECRYPT_MODE, aPrivate);
        byte[] bytes = cipher.doFinal(encryptedBytes);

        return new String(bytes);
    }

    private byte[] encrypt(KeyPair keyPair) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        return cipher.doFinal(plainText.getBytes());
    }


    private KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyPairGenerator.initialize(1024, secureRandom);

        return keyPairGenerator.generateKeyPair();
    }




}
