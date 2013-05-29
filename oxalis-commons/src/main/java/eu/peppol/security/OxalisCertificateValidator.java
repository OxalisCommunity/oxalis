package eu.peppol.security;

import eu.peppol.start.identifier.KeystoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.*;
import java.util.Arrays;

/**
 * Singleton and thread safe X.509 Certificate validator.
 *
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
     * Validates the supplied certificate against the PEPPOL chain of trust.
     *
     *
     * @param x509Certificate
     * @throws CertPathValidatorException if the supplied certificate fails validation.
     */
    public boolean validate(X509Certificate x509Certificate) throws CertPathValidatorException {

        String certificateInfo = x509Certificate.getSerialNumber() + " " + x509Certificate.getSubjectDN().getName();

        log.debug("Validation of certificate " + certificateInfo + " requested");

        if (cache.isKnownValidCertificate(x509Certificate.getSerialNumber())) {
            log.debug("Certificate " + certificateInfo + " found in cache of trusted certificates.");
            return true;
        }
        log.debug("Certificate " + certificateInfo + " not found in cache, performing OCSP and CRLDP (optional) validation");

        PKIXParameters pkixParameters = null;
        try {
            pkixParameters = new PKIXParameters(KeystoreManager.getInstance().getPeppolTruststore());
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
        }
    }
}
