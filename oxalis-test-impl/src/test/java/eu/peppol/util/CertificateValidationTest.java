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

package eu.peppol.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Verifies that our dummy AP certificate can be validated against our dummy CA certificate.
 *
 * @author steinar
 *         Date: 20.12.2015
 *         Time: 11.10
 */
public class CertificateValidationTest {

    @BeforeClass
    public void setUp() {
        // Installs the Bouncy Castle provider
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void verifyDummyCertificates() {

        KeyStore keystore = loadKeystore("security/oxalis-dummy-keystore.jks","peppol");
        try {
            Enumeration<String> aliases = keystore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);
                validateCertificate(certificate);
            }
        } catch (KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    private KeyStore loadKeystore(String resourceName, String password) {

        try (InputStream is = CertificateValidationTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(is, password.toCharArray());
            return keyStore;
        } catch (NoSuchAlgorithmException | IOException | KeyStoreException | CertificateException e) {
            throw new IllegalStateException("Unable to load keystore " + resourceName + ", " +e.getMessage(),e);
        }
    }

    public void validateCertificate(X509Certificate certificate) {

        try {

            List<X509Certificate> certificateList = new ArrayList<X509Certificate>();
            certificateList.add(certificate);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509",  new BouncyCastleProvider());
            CertPath certPath = certificateFactory.generateCertPath(certificateList);

            KeyStore trustStore = loadKeystore("security/oxalis-dummy-ca.jks","peppol");

            // Create the parameters for the validator
            PKIXParameters params = new PKIXParameters(trustStore);

            // Disable revocation checking as we trust our own truststore (and do not have a CRL and don't want OCSP)
            params.setRevocationEnabled(false);

            // Validate the certificate path
            CertPathValidator pathValidator = CertPathValidator.getInstance("PKIX",new BouncyCastleProvider());
            CertPathValidatorResult validatorResult = pathValidator.validate(certPath, params);

            // Get the CA used to validate this path
            PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) validatorResult;
            TrustAnchor ta = result.getTrustAnchor();
            X509Certificate trustCert = ta.getTrustedCert();

        } catch (Exception e) {
            throw new IllegalStateException("Unable to trust the signer : " + e.getMessage(), e);
        }
    }




}
