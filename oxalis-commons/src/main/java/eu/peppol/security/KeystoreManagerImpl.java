package eu.peppol.security;

import com.google.inject.Inject;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.cert.X509Certificate;

/**
 * Singleton, thread safe handler of operations related to <em>our</em> PEPPOL key and trust stores.
 *
 * Author: nigel
 * Date: Oct 9, 2011
 * Time: 4:01:31 PM
 *
 * @author steinar@sendregning.no
 */
public class KeystoreManagerImpl implements KeystoreManager {

    private static final Logger log = LoggerFactory.getLogger(KeystoreManagerImpl.class);

    /**
     * Holds the keystore containing our (this access point) private key and public certificate.
     */
    private final KeyStore ourKeystore;
    /**
     * Holds the PEPPOL trust store, which contains the intermediate certificates and root certificates of PEPPOL
     */
    private KeyStore peppolTrustedKeyStore;

    private PrivateKey privateKey;

    private GlobalConfiguration globalConfiguration;
    private final PeppolTrustStore peppolTrustStore;

    @Inject
    KeystoreManagerImpl(GlobalConfiguration globalConfiguration, PeppolTrustStore peppolTrustStore) {
        this.globalConfiguration = globalConfiguration;
        this.peppolTrustStore = peppolTrustStore;

        peppolTrustedKeyStore = loadPeppolTruststore();

        String keyStorePassword = globalConfiguration.getKeyStorePassword();
        ourKeystore = loadOurKeystore(keyStorePassword);
        privateKey = getOurPrivateKey(ourKeystore, keyStorePassword);
    }


    /**
     * Private constructor
     * @return
     */
    @Override
    public KeyStore loadOurKeystore(String password) {
        String keyStoreFileName = globalConfiguration.getKeyStoreFileName();
        log.debug("Loading PEPPOL keystore from " + keyStoreFileName);
        return KeyStoreUtil.loadJksKeystore(keyStoreFileName, password);
    }


    /**
     * Provides the currently loaded PEPPOL trust store holding the root and intermediate certificates.
     * The actual key store loaded, depends upon the global configuration.
     *
     * @return currently loaded truststore.
     */
    @Override
    public KeyStore getPeppolTrustedKeyStore() {
        if (peppolTrustStore == null) {
            throw new IllegalStateException("Truststore not loaded from disk");
        }
        return peppolTrustedKeyStore;
    }

    /**
     * Provides this PEPPOL Access Point's keystore, which holds the private key and the public certificate
     * issued by a PEPPOL authority. The physical location is referenced in the global configuration.
     *
     * @return the KeyStore holding the private key and certificate (with public key) of this access point
     */
    @Override
    public KeyStore getOurKeystore() {
        if (ourKeystore == null) {
            throw new IllegalStateException("KeystoreManagerImpl not properly initialized");
        }
        return ourKeystore;
    }


    /**
     * Retrieves the Access Point's certificate from the currently loaded keystore.
     *
     * @return the X.509 certificate identifying this access point
     */
    @Override
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
    @Override
    public CommonName getOurCommonName() {
        return CommonName.valueOf(getOurCertificate().getSubjectX500Principal());
    }


    @Override
    public PrivateKey getOurPrivateKey() {
        return privateKey;
    }



    @Override
    public PrivateKey getOurPrivateKey(KeyStore keyStore, String password) {
        try {
            String alias = keyStore.aliases().nextElement();
            Key key = keyStore.getKey(alias, password.toCharArray());

            if (key instanceof PrivateKey) {
                return (PrivateKey) key;
            } else {
                throw new RuntimeException("Private key must be first element in our keystore at " + globalConfiguration.getKeyStoreFileName() + " " + key.getClass());
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
    @Override
    public KeyStore loadPeppolTruststore() {

        PkiVersion pkiVersion = globalConfiguration.getPkiVersion();
        OperationalMode modeOfOperation = globalConfiguration.getModeOfOperation();
        KeyStore keyStore = peppolTrustStore.loadTrustStoreFor(pkiVersion, modeOfOperation);

        return keyStore;
    }

    @Override
    public boolean isOurCertificate(X509Certificate candidate) {
        X509Certificate ourCertificate = getOurCertificate();
        return ourCertificate.getSerialNumber().equals(candidate.getSerialNumber());
    }
}
