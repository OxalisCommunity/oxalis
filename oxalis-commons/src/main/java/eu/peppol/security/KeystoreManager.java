package eu.peppol.security;

import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.cert.X509Certificate;

/**
 * Singleton, thread safe handler of operations related to <em>our</em> PEPPOL key and trust stores.
 *
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

        peppolTrustStore = loadPeppolTruststore();

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
        log.debug("Loading PEPPOL keystore from " + keyStoreFileName);
        return KeyStoreUtil.loadJksKeystore(keyStoreFileName, password);
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

    /**
     * Retrieves the Common Name attribute (CN=) of our certificate.
     *
     * @return the Common Name, without the CN= prefix, of our certificate
     */
    public CommonName getOurCommonName() {
        return new CommonName(getOurCertificate().getSubjectX500Principal());
    }


    public PrivateKey getOurPrivateKey() {
        return privateKey;
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

    /**
     * Loads the PEPPOL trust store from disk. The PEPPOL trustore holds the PEPPOL intermediate and root certificates.
     */
    KeyStore loadPeppolTruststore() {

        PeppolTrustStore store = new PeppolTrustStore();
        PkiVersion pkiVersion = GlobalConfiguration.getInstance().getPkiVersion();
        OperationalMode modeOfOperation = GlobalConfiguration.getInstance().getModeOfOperation();
        KeyStore keyStore = store.loadTrustStoreFor(pkiVersion, modeOfOperation);

        return keyStore;
    }

    public boolean isOurCertificate(X509Certificate candidate) {
        X509Certificate ourCertificate = getOurCertificate();
        return ourCertificate.getSerialNumber().equals(candidate.getSerialNumber());
    }
}
