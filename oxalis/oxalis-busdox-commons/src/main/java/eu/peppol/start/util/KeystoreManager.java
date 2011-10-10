package eu.peppol.start.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;

/**
 * User: nigel
 * Date: Oct 9, 2011
 * Time: 4:01:31 PM
 */
public class KeystoreManager {

    private static Configuration configuration = Configuration.getInstance();
    private static String keystoreLocation = configuration.getProperty("keystore");
    private static String keystorePassword = configuration.getProperty("keystore.password");

    private KeyStore getKeystore() {
        return getKeystore(keystoreLocation, keystorePassword);
    }

    @SuppressWarnings({"ConstantConditions"})
    private KeyStore getKeystore(String location, String password) {

        InputStream inputStream = null;

        try {

            inputStream = new FileInputStream(location);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(inputStream, password.toCharArray());
            return keyStore;

        } catch (Exception e) {

            throw new RuntimeException("Failed to open keystore " + location, e);

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

    public PrivateKey getOurPrivateKey() {

        try {

            KeyStore keystore = getKeystore();
            String alias = keystore.aliases().nextElement();
            Key key = keystore.getKey(alias, keystorePassword.toCharArray());

            if (key instanceof PrivateKey) {
                return (PrivateKey) key;
            } else {
                throw new RuntimeException("Private key is not first element in keystore at " + keystoreLocation);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to get our private key", e);
        }
    }

    public TrustAnchor getTrustAnchor() {

        try {

            String location = configuration.getProperty("truststore");
            String password = configuration.getProperty("truststore.password");
            KeyStore truststore = getKeystore(location, password);
            String alias = configuration.getProperty("peppol.access.point.certificate.alias");
            return new TrustAnchor((X509Certificate) truststore.getCertificate(alias), null);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get the PEPPOL access point certificate", e);
        }
    }
}
