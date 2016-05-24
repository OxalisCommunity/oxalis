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

/**
 * @author steinar
 *         Date: 17.09.13
 *         Time: 14:54
 */


import org.testng.annotations.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

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
                new String("J2EE Security for Servlets, EJBs and Web Services "+ "\u00e6" + "\u00f8" + "\u00e5" + "\u00c6" + "\u00d8" + "\u00c5").getBytes();

        byte[] encBytes = encrypt(dataBytes, key, xform);
        byte[] decBytes = decrypt(encBytes, key, xform);

        boolean expected = java.util.Arrays.equals(dataBytes, decBytes);
        System.out.println("Test " + (expected ? "SUCCEEDED!" : "FAILED!"));
    }

}
