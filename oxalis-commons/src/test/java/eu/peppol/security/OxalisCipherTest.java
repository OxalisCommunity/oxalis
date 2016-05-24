/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.security;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Arrays;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 06.05.13
 *         Time: 22:34
 */
public class OxalisCipherTest {

    StatisticsKeyTool statisticsKeyTool;
    private OxalisCipher oxalisCipher;
    private OxalisCipherConverter oxalisCipherConverter;

    @BeforeMethod
    public void setUp() throws IOException {
        oxalisCipher = new OxalisCipher();
        oxalisCipherConverter = new OxalisCipherConverter();

        Path tempDirectory = Files.createTempDirectory("unit-test");

        statisticsKeyTool = new StatisticsKeyTool();
        assertNotNull(statisticsKeyTool);
    }


    /**
     * Verifies understanding of converting a string between bytes and a string
     */
    @Test
    public void convertBetweenCharAndBytes() throws Exception {

        String s = "Hello World!" + "\u00e6" + "\u00f8" + "\u00e5" + "\u00c6" + "\u00d8" + "\u00c5";

        byte[] bytes = s.getBytes("UTF-8");

        String s1 = new String(bytes, "UTF-8");
        assertEquals(s, s1);
    }


    @Test
    public void encryptAndDecryptAString() throws Exception {
        String s = "Hello World! " + "\u00e6" + "\u00f8" + "\u00e5" + "\u00c6" + "\u00d8" + "\u00c5";

        // Encrypt, decrypt and compare using the simple methods
        byte[] encryptedBytes = oxalisCipher.encrypt(s.getBytes("UTF-8"));
        byte[] decryptedBytes = oxalisCipher.decrypt(encryptedBytes);

        // array of bytes should be equal
        assertTrue(Arrays.equals(s.getBytes("UTF-8"), decryptedBytes));

        // Converting back to a string should still equal our initial string
        String s2 = new String(decryptedBytes, "UTF-8");
        assertEquals(s, s2, "Oops decrypted string does not compare to encrypted string. Perhaps an encoding problem?");
    }

    @Test
    public void encryptAndDecryptSomeXml() throws Exception {
        String s = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<peppol-ap-statistics start=\"2013-11-30 23:00\" end=\"2013-11-30 23:00\"></peppol-ap-statistics>";

        // Encrypt, decrypt and compare using the simple methods
        byte[] encryptedBytes = oxalisCipher.encrypt(s.getBytes("UTF-8"));
        byte[] decryptedBytes = oxalisCipher.decrypt(encryptedBytes);

        // array of bytes should be equal
        assertTrue(Arrays.equals(s.getBytes("UTF-8"), decryptedBytes));

        // Converting back to a string should still equal our initial string
        String s2 = new String(decryptedBytes, "UTF-8");
        assertEquals(s, s2);
    }


    /**
     * Mimics how a servlet will respond with an encrypted entity  for which the key is encrypted and placed
     * in a header.
     */
    @Test
    public void testEncryptAndDecrypt() throws IOException, BadPaddingException, IllegalBlockSizeException {

        String plainText = "Hello World! " + "\u00e6" + "\u00f8" + "\u00e5";

        byte[] encryptedBytes = encryptString(plainText, Charset.forName("ISO-8859-1"));

        String s = decryptToString(oxalisCipher, encryptedBytes, Charset.forName("ISO-8859-1"));

        assertEquals(s, plainText);
    }

    /**
     * Decrypts bytes using the symmetric key held in the OxalisCipher instance.
     * Uses Cipher streams.
     *
     * @param cipher
     * @param encryptedBytes
     * @param charset the character set encoding to use
     * @return
     * @throws IOException
     */
    private String decryptToString(OxalisCipher cipher, byte[] encryptedBytes, Charset charset) throws IOException, BadPaddingException, IllegalBlockSizeException {

        byte[] decryptedBytes = oxalisCipher.decrypt(encryptedBytes);

        return new String(decryptedBytes, charset);
    }

    /**
     * Encrypts bytes using the symmetric key held in the OxalisCipher instance.
     */
    private byte[] encryptString(String plainText, Charset charset) throws IOException, BadPaddingException, IllegalBlockSizeException {

        return oxalisCipher.encrypt(plainText.getBytes(charset));

    }


    /**
     * Encrypts data using our symmetric secret key obtained from the instance of OxalisCipher, after which
     * the secret key is encrypted (wrapped) using the public asymmetric RSA keys loaded from disk.
     * Finally the secret key is decrypted (unwrapped) and the encrypted data is decrypted.
     * <p>
     * NOTE! If this goes belly up, you should verify that the public and private key loaded by
     * StatisticsKeyTool is actually a pair.
     *
     * @param publicKey
     * @throws Exception
     */
    @Test(groups = {"integration"}, dataProvider = "publicKey", enabled = true)
    public void encryptDataEncryptKeyAndReverse(PublicKey publicKey) throws Exception {

        String plainText = "Sample data for testing purposes æøå";
        byte[] encryptedBytes = encryptString(plainText, Charset.forName("UTF-8"));

        String encodedSymmetricKey = oxalisCipherConverter.getWrappedSymmetricKeyAsString(publicKey, oxalisCipher);
        assertNotNull(encodedSymmetricKey);
    }

    /**
     * Proves that a symmetric AES key, which we obtain from the OxalisCipher instance,
     * can be wrapped and unwrapped using an asymmetric RSA key.
     *
     * @throws Exception
     */
    @Test
    public void testWrapKey() throws Exception {
        // Generates our asymmetric key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Wraps the symmetric secret key
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.WRAP_MODE, keyPair.getPublic());
        byte[] wrappedSecretKey = cipher.wrap(oxalisCipher.getSecretKey());


        // Unwraps the symmetric secret key
        Cipher cipher1 = Cipher.getInstance("RSA");
        cipher1.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
        SecretKey aes = (SecretKey) cipher1.unwrap(wrappedSecretKey, "AES", Cipher.SECRET_KEY);

    }


    @DataProvider(name = "publicKey")
    public Object[][] createKeyPair() {

        return new Object[][]{
                {statisticsKeyTool.loadPublicKeyFromClassPath()}
        };
    }
}
