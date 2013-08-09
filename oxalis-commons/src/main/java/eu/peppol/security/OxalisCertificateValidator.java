package eu.peppol.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.cert.*;
import java.util.Arrays;

/**
 * Singleton and thread safe X.509 Certificate validator specifically designed to validate
 * PEPPOL certificates.
 *
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
public enum OxalisCertificateValidator {

    INSTANCE;

    public static final Logger log = LoggerFactory.getLogger(OxalisCertificateValidator.class);
    private final CertificateFactory certificateFactory;

    public static final OcspValidatorCache cache = OcspValidatorCache.getInstance();

    OxalisCertificateValidator() {
        // Seems the CertificateFactory is the only thread safe object around here :-)
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new IllegalStateException("Unable to create CertificateFactory " + e.getMessage(), e);
        }
    }


    public static OxalisCertificateValidator getInstance() {
        return INSTANCE;
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
        KeyStore peppolTrustStore = KeystoreManager.getInstance().getPeppolTruststore();

        return validateUsingCache(x509Certificate, peppolTrustStore);
    }

    /**
     * Validates the supplied certificate against the PEPPOL chain of trust supplied. However; first
     * the internal cache of previously verified certificates is checked.
     *
     * @param x509Certificate
     * @throws CertPathValidatorException if the supplied certificate fails validation.
     */
    public boolean validateUsingCache(X509Certificate x509Certificate, KeyStore peppolTrustStore) {
        return doValidation(x509Certificate, peppolTrustStore, true);
    }

    /**
     * Same as #validateUsingCache, except that the local cache is ignored. I.e. the validation is always performed.
     */
    public boolean validateWithoutCache(X509Certificate x509Certificate, KeyStore peppolTrustStore) {
        return doValidation(x509Certificate, peppolTrustStore, false);
    }

    /**
     * Helper method, which performs the grunt work.
     */
    boolean doValidation(X509Certificate x509Certificate, KeyStore peppolTrustStore, boolean checkInCache) {

        String certificateInfo = x509Certificate.getSerialNumber() + " " + x509Certificate.getSubjectDN().getName();
        log.debug("Validation of certificate " + certificateInfo + " requested");


        if (checkInCache && hasEntryInValidatedCache(x509Certificate)) return true;

        log.debug("Performing OCSP and CRLDP (optional) validation");

        PKIXParameters pkixParameters = null;
        try {
            pkixParameters = new PKIXParameters(peppolTrustStore);
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Unable to create PKIXParameters from current PEPPOL truststore" + e.getMessage(), e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Unable to create PKIXParameters; " + e.getMessage(), e);
        }

        pkixParameters.setRevocationEnabled(true);
        Security.setProperty("ocsp.enable", "true");

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

            // Insert serial number of this certificate to improve performance
            cache.setKnownValidCertificate(x509Certificate.getSerialNumber());

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


    boolean hasEntryInValidatedCache(X509Certificate x509Certificate) {

        if (cache.isKnownValidCertificate(x509Certificate.getSerialNumber())) {
            String certificateInfo = x509Certificate.getSerialNumber() + " " + x509Certificate.getSubjectDN().getName();
            log.debug("Certificate " + certificateInfo + " found in cache of trusted certificates.");
            return true;
        }
        return false;
    }


}
