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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Provides various methods for generation, loading and saving private and public keys.
 *
 * @author steinar
 *         Date: 01.05.13
 *         Time: 21:17
 */
public class StatisticsKeyTool {

    public static final String ASYMMETRIC_KEY_ALGORITHM = "RSA";
    public static final String OXALIS_STATISTICS_PUBLIC_KEY = "oxalis-statistics-public.key";
    public static final int MAX_LENGTH_OF_ENCODED_KEY = 4096;

    public static final Logger log = LoggerFactory.getLogger(StatisticsKeyTool.class);


    /**
     * The Oxalis statistics public key is supplied as part of the distribution (of course).
     *
     * @return the statistics public key
     */
    public PublicKey loadPublicKeyFromClassPath() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(OXALIS_STATISTICS_PUBLIC_KEY);
        if (inputStream == null) {
            throw new IllegalStateException("Unable to locate file " + OXALIS_STATISTICS_PUBLIC_KEY);
        }
        try {
            byte[] bytes = loadBytesFrom(inputStream);
            log.info("Loaded public key with " + bytes.length + " bytes");
            return publicKeyFromBytes(bytes);

        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Invalid public key encoded in " + OXALIS_STATISTICS_PUBLIC_KEY, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to close input stream for " + OXALIS_STATISTICS_PUBLIC_KEY, e);
            }
        }
    }

    /**
     * Loads the public key used for encrypting statistical data.
     *
     * @param file
     * @return
     */
    public PublicKey loadPublicKey(File file) {
        try {
            byte[] encodedPublicKey = loadBytesFromFile(file);

            return publicKeyFromBytes(encodedPublicKey);

        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to create key from encoded specification in " + file.getAbsolutePath() + "; " + e.getMessage(), e);
        }

    }



    File getPublicKeyFile() {
        return new File(getResourceDirectory(), OXALIS_STATISTICS_PUBLIC_KEY);
    }


    /**
     * Provides the name of the temporary directory into which generated keys will be stored.
     *
     * @return
     */
    File getResourceDirectory() {
        String tempDirName = System.getProperty("java.io.tmpdir");
        return new File(tempDirName);
    }


    private PublicKey publicKeyFromBytes(byte[] encodedPublicKey) throws InvalidKeySpecException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encodedPublicKey);

        KeyFactory keyFactory = createKeyFactory();
        return keyFactory.generatePublic(x509EncodedKeySpec);
    }


    private byte[] loadBytesFromFile(File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);

            return loadBytesFrom(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to open file " + file.getAbsolutePath());
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to close file " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    private byte[] loadBytesFrom(InputStream inputStream) {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[MAX_LENGTH_OF_ENCODED_KEY];

        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toByteArray();

    }

    private KeyFactory createKeyFactory() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_KEY_ALGORITHM);
            return keyFactory;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to create key factory with algorithm " + ASYMMETRIC_KEY_ALGORITHM + "; " + e.getMessage(), e);
        }
    }

}
