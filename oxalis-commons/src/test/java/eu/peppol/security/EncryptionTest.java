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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.Charset;
import java.security.*;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 29.04.13
 *         Time: 09:51
 */
public class EncryptionTest {


    private KeyPair keyPair;
    private SecretKey secretKey;


    @BeforeClass
    public void createKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);  // Medium security is fine
        keyPair = keyPairGenerator.generateKeyPair();

        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        keyGenerator.init(new SecureRandom());
        secretKey = keyGenerator.generateKey();
    }


    @Test()
    public void encryptData() throws Exception {

        String plainText = "Hello world";
        byte[] encryptedBytes = encrypt(keyPair, plainText);

        String decryptedPlainText = decrypt(keyPair, encryptedBytes);

        assertEquals(decryptedPlainText, plainText);

    }


    @Test
    public void encryptAndDecryptFile() throws Exception {

        File file = File.createTempFile("oxalis", ".data");

        // Writes encrypted data into file
        Cipher encryptionCipher = Cipher.getInstance("DES/CFB8/NoPadding");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey);


        CipherOutputStream cipherOutputStream = new CipherOutputStream(new FileOutputStream(file), encryptionCipher);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(cipherOutputStream, Charset.forName("UTF-8"));

        String plainText = "Hello world";
        outputStreamWriter.write(plainText);
        outputStreamWriter.close();


        // Reads and decrypts data from a file
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Cipher cipher = Cipher.getInstance("DES/CFB8/NoPadding");
        AlgorithmParameters parameters = encryptionCipher.getParameters();
        System.err.println(parameters.toString());
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameters);

        Cipher symmetricCipher = cipher;
        CipherInputStream cipherInputStream = new CipherInputStream(new FileInputStream(file), symmetricCipher);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(cipherInputStream, Charset.forName("UTF-8")));

        String decryptedText = bufferedReader.readLine();
        assertEquals(decryptedText, plainText);

        file.delete();
    }


    /**
     * Experimental test method. Does not test any part of the system
     *
     * @throws Exception
     */
    @Test
    public void encryptDataWithWrappedKey() throws Exception {

        // Creates the shared key to be used for encrypting the data
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        Key sharedKey = keyGenerator.generateKey();

        // Wraps the shared key within an asymmetric key
        String password = "The quick brown fox jumped over the lazy dog";

        byte[] salt = {0x01, 0x02, 0x03, 0x04, 0x05, 0x07, 0x06, 0x08};

        PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(salt, 20);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());

        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey passwordKey = kf.generateSecret(pbeKeySpec);

        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(Cipher.WRAP_MODE, passwordKey, pbeParameterSpec);
        byte[] wrappedSharedKey = cipher.wrap(sharedKey);


        // Encrypt some data with shared key
        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sharedKey);

        String plainText = "Hello world";
        byte[] input = plainText.getBytes();
        byte[] encrypted = cipher.doFinal(input);

        // Read the wrapped key and the encrypted data

        // First; unwrap the key
        cipher = Cipher.getInstance("PBEWithMD5AndDES");
        // TODO: pass the parameters of the wrappedSharedKey
        cipher.init(Cipher.UNWRAP_MODE, passwordKey, pbeParameterSpec);

        Key unwrappedKey = cipher.unwrap(wrappedSharedKey, "AES", Cipher.SECRET_KEY);

        // Decrypt the data
        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, unwrappedKey);

        String newData = new String(cipher.doFinal(encrypted));

        assertEquals(newData, plainText);
    }


    @Test
    public void createAsymmetricKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair pair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = pair.getPublic();
        System.err.println("Algorithm:" + publicKey.getAlgorithm());
        System.err.println("Format" + publicKey.getFormat());

        byte[] encoded = publicKey.getEncoded();
        for (int i = 0; i < encoded.length; i++) {
            byte b = encoded[i];
        }
    }

    /**
     * Encrypts something with AES, transforms the key into a key specification, recreates the key and decrypts
     * the encrypted contents.
     *
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    @Test
    public void encryptAndDecryptWithSymmetricKey() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecretKey key = keyGenerator.generateKey();
        System.err.println("Algorithm:" + key.getAlgorithm());
        System.err.println("Format:" + key.getFormat());

        // Transforms the secret key (symmetric) into a secret key specification
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");

        Cipher encryptionCipher = Cipher.getInstance("AES");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key);

        String plainText = "Hello world";

        byte[] encryptedBytes = encryptionCipher.doFinal(plainText.getBytes("UTF-8"));

        Cipher decryptionCipher = Cipher.getInstance("AES");
        decryptionCipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        byte[] decryptedBytes = decryptionCipher.doFinal(encryptedBytes);

        String decryptedText = new String(decryptedBytes, Charset.forName("UTF-8"));

        assertEquals(decryptedText, plainText);
    }


    @Test
    public void holdEncryptedBytes() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecretKey key = keyGenerator.generateKey();
    }

    private Cipher getSymmetricCipher(int mode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("DES/CFB8/NoPadding");
        cipher.init(mode, secretKey);

        return cipher;
    }

    private String decrypt(KeyPair keyPair, byte[] encryptedBytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = getCipher();
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

        byte[] bytes = cipher.doFinal(encryptedBytes);

        return new String(bytes, Charset.forName("UTF-8"));
    }

    private Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance("RSA");
    }

    private byte[] encrypt(KeyPair keyPair, String plainText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        byte[] bytes = cipher.doFinal(plainText.getBytes());

        return bytes;
    }
}
