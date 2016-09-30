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

/* Created by steinar on 14.05.12 at 00:38 */
package eu.peppol.security;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Iterator;

/**
 * Finds and returns a key using the data contained in a {@link KeyInfo} object
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 * @see  <a href="http://java.sun.com/developer/technicalArticles/xml/dig_signature_api/">Programming with the Java XML Digital Signature API</a>
 */
public class X509KeySelector extends KeySelector {

    /**
     * Invoked whenever the caller needs to retrieve the key.
     * Note to self; how can they make it so damned complicated?
     */
    public KeySelectorResult select(KeyInfo keyInfo,
                                    KeySelector.Purpose purpose,
                                    AlgorithmMethod method,
                                    XMLCryptoContext context)
            throws KeySelectorException {

        Iterator ki = keyInfo.getContent().iterator();
        while (ki.hasNext()) {
            XMLStructure info = (XMLStructure) ki.next();
            if (!(info instanceof X509Data))
                continue;
            X509Data x509Data = (X509Data) info;
            Iterator xi = x509Data.getContent().iterator();
            while (xi.hasNext()) {
                Object o = xi.next();
                if (!(o instanceof X509Certificate))
                    continue;

                final PublicKey key = ((X509Certificate) o).getPublicKey();
                // Make sure the algorithm is compatible
                // with the method.
                if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
                    X509Certificate x509Certificate = (X509Certificate) o;
                    try {
                        // Ensures the certificate is valid for current date
                        x509Certificate.checkValidity();
                    } catch (CertificateExpiredException e) {
                        throw new KeySelectorException("Certificate of SMP has expired ", e);
                    } catch (CertificateNotYetValidException e) {
                        throw new KeySelectorException("Certificate of SMP not yet valid", e);
                    }
                    return new KeySelectorResult() {
                        public Key getKey() {

                            return key;
                        }
                    };
                }
            }
        }
        throw new KeySelectorException("No key found!");
    }


    static boolean algEquals(String algURI, String algName) {
        if ((algName.equalsIgnoreCase("DSA") &&
                algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) ||
                (algName.equalsIgnoreCase("RSA") &&
                        algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1))) {
            return true;
        } else {
            return false;
        }
    }
}