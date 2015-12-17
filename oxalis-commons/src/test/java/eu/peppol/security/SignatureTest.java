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

import org.testng.annotations.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

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
