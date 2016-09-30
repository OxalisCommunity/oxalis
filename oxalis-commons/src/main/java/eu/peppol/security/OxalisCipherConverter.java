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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author steinar
 *         Date: 14.05.13
 *         Time: 09:57
 */
public class OxalisCipherConverter {

    /**
     * Creates an instance of OxalisCipher:
     * <ol>
     *     <li>Decodes the supplied hex string representation of a wrapped key into an array of bytes representation</li>
     *     <li>Creates a cipher, which is initialized with a private key</li>
     *     <li>Unwraps (decrypts) the secret key represented by an array of bytes into a SecretKey</li>
     *     <li>Creates an OxalisCipher using the unwrapped SecretKey</li>
     * </ol>
     * @param wrappedSymmetricKeyAsHexString
     * @param privateKey
     * @return
     */
    public OxalisCipher createCipherFromWrappedHexKey(String wrappedSymmetricKeyAsHexString, PrivateKey privateKey) {

        // 1) Decodes the hex string representation of a wrapped key
        byte[] encodedBytes = encodedBytesFromHexString(wrappedSymmetricKeyAsHexString);

        try {
            // 2) Creates the Cipher using supplied private key
            Cipher cipher = Cipher.getInstance(StatisticsKeyTool.ASYMMETRIC_KEY_ALGORITHM);
            cipher.init(Cipher.UNWRAP_MODE, privateKey);

            // 3) Unwraps (decrypts) the secret key using our private key
            SecretKey secretKey = (SecretKey) cipher.unwrap(encodedBytes, OxalisCipher.SYMMETRIC_KEY_ALGORITHM, Cipher.SECRET_KEY);

            // 4) creates the Oxalis cipher
            OxalisCipher oxalisCipher = new OxalisCipher(secretKey);
            return oxalisCipher;

        } catch (NoSuchAlgorithmException e) {
            throw new UnwrapSymmetricKeyException(wrappedSymmetricKeyAsHexString,e);
        } catch (NoSuchPaddingException e) {
            throw new UnwrapSymmetricKeyException(wrappedSymmetricKeyAsHexString, e);
        } catch (InvalidKeyException e) {
            throw new UnwrapSymmetricKeyException(wrappedSymmetricKeyAsHexString, e);
        }
    }

    /**
     * Encrypts the secret key (symmetric key) held inside the OxalisCipher instance using the supplied PublicKey, after
     * which the resulting wrapped secret key is transformed into a hex string suitable for transmission, persistence etc.
     *
     * @param publicKey the public asymmetric key to use for encrypting the secret symmetric key
     * @param oxalisCipher the instance of OxalisCipher in which the secret symmetric key is held.
     * @return
     */
    public String getWrappedSymmetricKeyAsString(PublicKey publicKey,OxalisCipher oxalisCipher) {

        try {
            Cipher cipher = Cipher.getInstance(StatisticsKeyTool.ASYMMETRIC_KEY_ALGORITHM);
            cipher.init(Cipher.WRAP_MODE, publicKey);
            SecretKey secretKey = oxalisCipher.getSecretKey();
            byte[] encodedBytes = cipher.wrap(secretKey);

            return new String(Hex.encodeHex(encodedBytes));

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to create cipher with algorithm: " + StatisticsKeyTool.ASYMMETRIC_KEY_ALGORITHM, e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException("Unable to create cipher with default padding for algorithm " + StatisticsKeyTool.ASYMMETRIC_KEY_ALGORITHM,e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("The public key is invalid " + e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException("Error during encryption of symmetric key: " + e.getMessage(), e);
        }
    }


    private byte[] encodedBytesFromHexString(String wrappedSymmetricKeyAsHexString) {
        byte[] encodedBytes;

        try {
            encodedBytes = Hex.decodeHex(wrappedSymmetricKeyAsHexString.toCharArray());
        } catch (DecoderException e) {
            throw new IllegalArgumentException("Unable to decode hex string " + wrappedSymmetricKeyAsHexString + "; " + e.getMessage(), e);
        }
        return encodedBytes;
    }
}
