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

import com.google.inject.Inject;
import eu.peppol.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.util.Arrays;

/**
 * Singleton and thread safe X.509 Certificate validator specifically designed to validate
 * PEPPOL certificates.
 * <p/>
 * <p/>
 * <p>Due to the fact that PKIXParameters and CertPath objects are not thread safe, the amount of initialization
 * performed in the constructor is kind of limited. If performance needs to be improved further, a pool should
 * be considered.
 * </p>
 *
 * @author steinar
 *         Date: 27.05.13
 *         Time: 12:39
 */
public class OxalisCertificateValidator {

    public static final Logger log = LoggerFactory.getLogger(OxalisCertificateValidator.class);
    private final CertificateFactory certificateFactory;

    public static final OcspValidatorCache cache = OcspValidatorCache.getInstance();
    private final KeystoreManager keystoreManager;
    private int cacheHits = 0;

    @Inject
    public OxalisCertificateValidator(KeystoreManager keystoreManager) {
        this.keystoreManager = keystoreManager;
        // Seems the CertificateFactory is the only thread safe object around here :-)
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new IllegalStateException("Unable to create CertificateFactory " + e.getMessage(), e);
        }
    }



    /**
     * Validates the supplied certificate against the PEPPOL chain of trust as configured in the
     * global configuration file.
     *
     * @param x509Certificate certificate to be validated.
     * @return true if valid, false otherwise
     * @throws CertPathValidatorException if thrown by the Java runtime.
     */
    public boolean validate(X509Certificate x509Certificate) {
        // Retrieves the trust store to be used for validation
        KeyStore peppolTrustStore = keystoreManager.getPeppolTrustedKeyStore();

        return validateUsingCache(x509Certificate, peppolTrustStore);
    }

    /**
     * Validates the supplied certificate against the PEPPOL chain of trust supplied. However; first
     * the internal cache of previously verified certificates is checked.
     *
     * @param x509Certificate
     * @throws CertPathValidatorException if the supplied certificate fails validation.
     */
    boolean validateUsingCache(X509Certificate x509Certificate, KeyStore peppolTrustStore) {
        return doValidation(x509Certificate, peppolTrustStore, true);
    }

    /**
     * Helper method, which performs the grunt work.
     */
    boolean doValidation(X509Certificate x509Certificate, KeyStore peppolTrustStore, boolean checkInCache) {

        String certificateInfo = certificateInfo(x509Certificate);
        log.debug("Validation of certificate " + certificateInfo + " requested");

        BigInteger thumbPrint = createThumbPrint(x509Certificate);

        if (checkInCache && hasEntryInValidatedCache(thumbPrint)) return true;

        log.debug("Performing optional OCSP and CRLDP validation");

        PKIXParameters pkixParameters = null;
        try {
            pkixParameters = new PKIXParameters(peppolTrustStore);
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Unable to create PKIXParameters from current PEPPOL truststore" + e.getMessage(), e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Unable to create PKIXParameters; " + e.getMessage(), e);
        }


        pkixParameters.setRevocationEnabled(false);  // TODO: determine when this should be activated and not

        // TODO: activate this if certificate contains OCSP url
        Security.setProperty("ocsp.enable", "false");   // Disable OCSP by default

        // Enables CRL Distribution Points extension, which is disabled by default for compatibility reasons
        System.setProperty("com.sun.security.enableCRLDP", "true");

        CertPathValidator certPathValidator = null;
        try {
            certPathValidator = CertPathValidator.getInstance("PKIX");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to create instance of certificate path valdiator");
        }

        try {

            CertPath certPath = certificateFactory.generateCertPath(Arrays.asList(x509Certificate));
            CertPathValidatorResult validatorResult = certPathValidator.validate(certPath, pkixParameters);

            // Get the CA used to validate this path
            PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) validatorResult;
            TrustAnchor ta = result.getTrustAnchor();
            X509Certificate trustCert = ta.getTrustedCert();

            log.debug("Certificate was signed by : {}", trustCert.getSubjectDN().toString());


            // Insert serial number of this certificate to improve performance
            cache.setKnownValidCertificate(thumbPrint);

            log.debug("Certificate " + certificateInfo + ", validated OK");
            return true;

        } catch (CertificateException e) {
            throw new IllegalStateException("Unable to establish cert path for certificate " + x509Certificate, e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Error during certificate validation: " + e.getMessage(), e);
        } catch (CertPathValidatorException e) {
            log.debug("Certificate " + certificateInfo + " failed validation: " + e.getMessage());
            // No need to throw an exception, simply return false
            return false;
        }
    }

    private String certificateInfo(X509Certificate x509Certificate) {
        return x509Certificate.getSerialNumber() + " " + x509Certificate.getSubjectDN().getName();
    }

    private BigInteger createThumbPrint(X509Certificate x509Certificate) {
        try {
            BigInteger thumbPrint = new BigInteger(1, Util.calculateSHA256(x509Certificate.getEncoded()));
            return thumbPrint;
        } catch (CertificateEncodingException e) {
            throw new IllegalStateException("Unable to encode certificate " + certificateInfo(x509Certificate) + " for thumbprint calculation ", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to calculate certificate thumbprint for certificate " + certificateInfo(x509Certificate), e);
        }
    }


    boolean hasEntryInValidatedCache(BigInteger certificateThumbPrint) {

        if (cache.isKnownValidCertificate(certificateThumbPrint)) {
            cacheHits++;
            log.debug("Certificate thumbprint found in cache of trusted certificates.");
            return true;
        }
        return false;
    }

    public int getCacheHits() {
        return cacheHits;
    }
}
