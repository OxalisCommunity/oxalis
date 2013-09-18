package eu.peppol.security;

/**
 * @author steinar
 *         Date: 17.09.13
 *         Time: 14:54
 */


import org.testng.annotations.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;

public class SymmetricCipherTest {
    private static byte[] iv =
            {0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d};

    private static byte[] encrypt(byte[] inpBytes,
                                  SecretKey key, String xform) throws Exception {
        Cipher cipher = Cipher.getInstance(xform);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(inpBytes);
    }

    private static byte[] decrypt(byte[] inpBytes,
                                  SecretKey key, String xform) throws Exception {
        Cipher cipher = Cipher.getInstance(xform);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(inpBytes);
    }

    @Test
    public void testCipher() throws Exception {
        //String xform = "DES/ECB/PKCS5Padding";
        String xform = "AES/ECB/PKCS5Padding";

        // Generate a secret key
        KeyGenerator kg = KeyGenerator.getInstance("AES");
//        kg.init(56); // 56 is the keysize. Fixed for DES
        SecretKey key = kg.generateKey();

        byte[] dataBytes =
                "J2EE Security for Servlets, EJBs and Web Services æøåÆØÅ".getBytes();

        byte[] encBytes = encrypt(dataBytes, key, xform);
        byte[] decBytes = decrypt(encBytes, key, xform);

        boolean expected = java.util.Arrays.equals(dataBytes, decBytes);
        System.out.println("Test " + (expected ? "SUCCEEDED!" : "FAILED!"));
    }

}
