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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Various generic methods useful for working with Java key stores.
 *
 * @author steinar
 *         Date: 08.08.13
 *         Time: 09:30
 */
public class KeyStoreUtil {

    public static final Logger log = LoggerFactory.getLogger(KeyStoreUtil.class);

    /**
     * Loads a JKS keystore according to the parameters supplied.
     *
     * @param location physical location, i.e. file name of JKS keystore
     * @param password password of keystore file.
     * @return
     */
    public static KeyStore loadJksKeystore(String location, String password) {

        File keyStoreFile = new File(location);

        return loadJksKeystore(keyStoreFile, password);
    }

    public static KeyStore loadJksKeystore(File keyStoreFile, String password) {
        try {
            log.debug("Attempting to open " + keyStoreFile);
            FileInputStream inputStream = new FileInputStream(keyStoreFile);

            return loadJksKeystoreAndCloseStream(inputStream, password);

        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed to open keystore " + keyStoreFile.getAbsolutePath(), e);
        }

    }

    /**
     * Convenience method for loading a JKS keystore.
     *
     * @param inputStream
     * @param password
     * @return
     */
    public static KeyStore loadJksKeystoreAndCloseStream(InputStream inputStream, String password) {
        try {

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(inputStream, password.toCharArray());
            return keyStore;

        } catch (CertificateException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to establish instance of KeyStore " + e.getMessage(), e);
        } catch (KeyStoreException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open keystore.");
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
            }
        }
    }
}
