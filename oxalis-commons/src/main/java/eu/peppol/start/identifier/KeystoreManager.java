package eu.peppol.start.identifier;

import eu.peppol.security.PkiVersion;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;

/**
 * Singleton, thread safe handler of operations related to our keystore and truststore.
 * <p/>
 * User: nigel
 * Date: Oct 9, 2011
 * Time: 4:01:31 PM
 *
 * @author steinar@sendregning.no
 */
public enum KeystoreManager {

    INSTANCE;


    Logger log;
    /**
     * Holds the keystore containing our (this access point) private key and public certificate.
     */
    private final KeyStore ourKeystore;
    /**
     * Holds the PEPPOL trust store, which contains the intermediate certificates and root certificates of PEPPOL
     */
    private final KeyStore peppolTrustStore;

    private PrivateKey privateKey;
    private GlobalConfiguration globalConfiguration;

    KeystoreManager() {
        log = LoggerFactory.getLogger(KeystoreManager.class);
        globalConfiguration = GlobalConfiguration.getInstance();

        peppolTrustStore = loadTruststore();

        String keyStorePassword = globalConfiguration.getKeyStorePassword();
        ourKeystore = loadOurKeystore(keyStorePassword);
        privateKey = getOurPrivateKey(ourKeystore, keyStorePassword);
    }


    /**
     * Private constructor
     * @return
     */
    KeyStore loadOurKeystore(String password) {
        String keyStoreFileName = globalConfiguration.getKeyStoreFileName();

        return loadJksKeystore(keyStoreFileName, password);
    }


    public static KeystoreManager getInstance() {
        return INSTANCE;
    }

    /**
     * Provides the currently loaded PEPPOL trust store holding the root and intermediate certificates.
     * The actual key store loaded, depends upon the global configuration.
     *
     * @return currently loaded truststore.
     */
    public KeyStore getPeppolTruststore() {
        if (peppolTrustStore == null) {
            throw new IllegalStateException("Truststore not loaded from disk");
        }
        return peppolTrustStore;
    }

    /**
     * Provides this PEPPOL Access Point's keystore, which holds the private key and the public certificate
     * issued by a PEPPOL authority. The physical location is referenced in the global configuration.
     *
     * @return the KeyStore holding the private key and certificate (with public key) of this access point
     */
    public KeyStore getOurKeystore() {
        if (ourKeystore == null) {
            throw new IllegalStateException("KeystoreManager not properly initialized");
        }
        return ourKeystore;
    }


    /**
     * Retrieves the Access Point's certificate from the currently loaded keystore.
     *
     * @return the X.509 certificate identifying this access point
     */
    public X509Certificate getOurCertificate() {

        try {
            KeyStore keystore = getOurKeystore();
            String alias = keystore.aliases().nextElement();
            return (X509Certificate) keystore.getCertificate(alias);

        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get our certificate from keystore", e);
        }
    }


    public PrivateKey getOurPrivateKey() {
        return privateKey;
    }


    /**
     * Loads a JKS keystore according to the parameters supplied.
     *
     * @param location physical location, i.e. file name of JKS keystore
     * @param password password of keystore file.
     * @return
     */
    KeyStore loadJksKeystore(String location, String password) {

        try {
            return loadJksKeystoreAndCloseStream(new FileInputStream(location), password);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to open keystore " + location, e);
        }
    }

    /**
     * Convenience method for loading a JKS keystore.
     *
     * @param inputStream
     * @param password
     * @return
     */
    KeyStore loadJksKeystoreAndCloseStream(InputStream inputStream, String password) {
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

    PrivateKey getOurPrivateKey(KeyStore keyStore, String password) {
        try {
            String alias = keyStore.aliases().nextElement();
            Key key = keyStore.getKey(alias, password.toCharArray());

            if (key instanceof PrivateKey) {
                return (PrivateKey) key;
            } else {
                throw new RuntimeException("Private key must be first element in our keystore at " + GlobalConfiguration.getInstance().getKeyStoreFileName() + " " + key.getClass());
            }
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Unable to access keystore: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to retrieve private key: " + e.getMessage(), e);
        } catch (UnrecoverableKeyException e) {
            throw new IllegalStateException("Unable to retrieve private key: " + e.getMessage(), e);
        }
    }

    public TrustAnchor getTrustAnchor() {

        try {

            KeyStore truststore = getPeppolTruststore();
            String alias = "ap";    // Uses the intermediate certificate as the trust anchor, rather than the root
            return new TrustAnchor((X509Certificate) truststore.getCertificate(alias), null);

        } catch (Exception e) {
            throw new RuntimeException("Failed to establish the PEPPOL access point TrustAnchor certificate", e);
        }
    }


    /**
     * Loads the PEPPOL trust store from disk. The PEPPOL trustore holds the PEPPOL intermediate and root certificates.
     */
    KeyStore loadTruststore() {

        String trustStoreResourceName = trustStoreResource();
        log.info("Loading truststore from  " + trustStoreResourceName);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(trustStoreResourceName);
        if (inputStream == null) {
            throw new IllegalStateException("Unable to load trust store resource " + trustStoreResourceName + " from class path");
        }

        return loadJksKeystoreAndCloseStream(inputStream, "peppol");
    }

    /**
     * Figures out the resource name of the trust store to be loaded given the current global configuration.
     * @return the resource name of the PEPPOL trust store to be loaded.
     */
    String trustStoreResource() {
        String trustStoreResourceName = "truststore.jks";
        PkiVersion pkiVersion = GlobalConfiguration.getInstance().getPkiVersion();

        switch (pkiVersion) {
            case V1:
                trustStoreResourceName = "truststore.jks";
                break;

            case V2:
                OperationalMode modeOfOperation = GlobalConfiguration.getInstance().getModeOfOperation();
                switch (modeOfOperation) {
                    case PRODUCTION:
                        trustStoreResourceName = "truststore-production.jks";
                        break;
                    case TEST:
                        trustStoreResourceName = "truststore-test.jks";
                        break;
                    default:
                        throw new IllegalStateException("Unknown mode of operation" + modeOfOperation.name());
                }
                break;
            default:
                throw new IllegalStateException("Unknown PKI version " + pkiVersion);
        }

        log.debug("trust store resource name: " + trustStoreResourceName);
        return trustStoreResourceName;
    }

    public boolean isOurCertificate(X509Certificate candidate) {
        X509Certificate ourCertificate = getOurCertificate();
        return ourCertificate.getSerialNumber().equals(candidate.getSerialNumber());
    }
}
