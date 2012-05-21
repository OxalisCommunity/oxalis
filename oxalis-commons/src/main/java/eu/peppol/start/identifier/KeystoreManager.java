package eu.peppol.start.identifier;

import eu.peppol.security.OcspValidatorCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Main manager for handling operations related to our keystore and truststore.
 * <p/>
 * User: nigel
 * Date: Oct 9, 2011
 * Time: 4:01:31 PM
 *
 * @author steinar@sendregning.no
 */
public class KeystoreManager {

    private static String keystoreLocation;
    private static String keystorePassword;
    private static KeyStore keyStore;
    private static KeyStore trustStore;
    private static PrivateKey privateKey;
    private CertPathValidator certPathValidator;
    private OcspValidatorCache ocspValidatorCache = new OcspValidatorCache();
    private PKIXParameters pkixParameters;

    public KeystoreManager() {
        initCertPathValidator();
    }

    public synchronized KeyStore getKeystore() {
        if (keyStore == null) {
            keyStore = getKeystore(keystoreLocation, keystorePassword);
        }

        return keyStore;
    }

    private KeyStore getKeystore(String location, String password) {

        try {

            return getKeystore(new FileInputStream(location), password);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to open keystore " + location, e);
        }
    }

    private KeyStore getKeystore(InputStream inputStream, String password) {
        try {

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(inputStream, password.toCharArray());
            return keyStore;

        } catch (Exception e) {

            throw new RuntimeException("Failed to open keystore", e);

        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
            }
        }
    }

    public X509Certificate getOurCertificate() {

        try {
            KeyStore keystore = getKeystore();
            String alias = keystore.aliases().nextElement();
            return (X509Certificate) keystore.getCertificate(alias);

        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get our certificate from keystore", e);
        }
    }

    public synchronized PrivateKey getOurPrivateKey() {

        if (privateKey == null) {
            try {

                KeyStore keystore = getKeystore();
                String alias = keystore.aliases().nextElement();
                Key key = keystore.getKey(alias, keystorePassword.toCharArray());

                if (key instanceof PrivateKey) {
                    privateKey = (PrivateKey) key;
                } else {
                    throw new RuntimeException("Private key is not first element in keystore at " + keystoreLocation);
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to get our private key", e);
            }
        }

        return privateKey;
    }

    public TrustAnchor getTrustAnchor() {

        try {

            KeyStore truststore = getTruststore();
            String alias = "ap";
            return new TrustAnchor((X509Certificate) truststore.getCertificate(alias), null);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get the PEPPOL access point certificate", e);
        }
    }

    public synchronized KeyStore getTruststore() {
        if (trustStore == null) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("truststore.jks");
            trustStore = getKeystore(inputStream, "peppol");
        }

        return trustStore;
    }

    public void initialiseKeystore(File keystoreFile, String keystorePassword) {
        if (keystoreFile == null) {
            throw new IllegalStateException("Keystore file not specified");
        }

        if (keystorePassword == null) {
            throw new IllegalStateException("Keystore password not specified");
        }

        if (!keystoreFile.exists()) {
            throw new IllegalStateException("Keystore file " + keystoreFile + " does not exist");
        }

        try {

            setKeystoreLocation(keystoreFile.getCanonicalPath());
            setKeystorePassword(keystorePassword);
            getKeystore();

        } catch (Exception e) {
            throw new IllegalArgumentException("Problem accessing keystore file", e);
        }
    }

    void initCertPathValidator() {
        Log.debug("Initialising OCSP validator");

        try {
            TrustAnchor trustAnchor = getTrustAnchor();
            certPathValidator = CertPathValidator.getInstance("PKIX");
            pkixParameters = new PKIXParameters(Collections.singleton(trustAnchor));
            pkixParameters.setRevocationEnabled(true);

            Security.setProperty("ocsp.enable", "true");
            Security.setProperty("ocsp.responderURL", "http://pilot-ocsp.verisign.com:80");

        } catch (Exception e) {
            throw new IllegalStateException("Unable to construct Certificate Path Validator; " + e, e);
        }
    }

    /**
     * Validates a X509 certificate using the OCSP services.
     *
     * @param certificate the certificate to be checked for validity
     * @return <code>true</code> if certificate is valid, <code>false</code> otherwise
     */
    public synchronized boolean validate(X509Certificate certificate) {

        BigInteger serialNumber = certificate.getSerialNumber();
        String certificateName = "Certificate " + serialNumber;
        Log.debug("Ocsp validation requested for " + certificateName + "\n\tSubject:" + certificate.getSubjectDN() + "\n\tIssued by:" + certificate.getIssuerDN());

        if (certPathValidator == null) {
            throw new IllegalStateException("Certificate Path validator not initialized");
        }

        if (ocspValidatorCache.isKnownValidCertificate(serialNumber)) {
            Log.debug(certificateName + " is OCSP valid (cached value)");
            return true;
        }

        try {

            List<Certificate> certificates = Arrays.asList(new Certificate[]{certificate});
            CertPath certPath = CertificateFactory.getInstance("X.509").generateCertPath(certificates);
            certPathValidator.validate(certPath, pkixParameters);
            ocspValidatorCache.setKnownValidCertificate(serialNumber);

            Log.debug(certificateName + " is OCSP valid");
            return true;

        } catch (Exception e) {
            Log.error(certificateName + " failed OCSP validation", e);
            return false;
        }
    }

    public boolean isOurCertificate(X509Certificate candidate) {
        X509Certificate ourCertificate = getOurCertificate();
        return ourCertificate.getSerialNumber().equals(candidate.getSerialNumber());
    }

    public static void setKeystoreLocation(String keystoreLocation) {
        KeystoreManager.keystoreLocation = keystoreLocation;
    }

    public static void setKeystorePassword(String keystorePassword) {
        KeystoreManager.keystorePassword = keystorePassword;
    }
}
