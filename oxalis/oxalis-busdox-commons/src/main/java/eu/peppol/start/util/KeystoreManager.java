package eu.peppol.start.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private static String keystoreLocation;
    private static String keystorePassword;

    public KeyStore getKeystore() {
        return getKeystore(keystoreLocation, keystorePassword);
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

            KeyStore truststore = getTruststore();
            String alias = "ap";
            return new TrustAnchor((X509Certificate) truststore.getCertificate(alias), null);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get the PEPPOL access point certificate", e);
        }
    }

    public KeyStore getTruststore() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("truststore.jks");
        return getKeystore(inputStream, "peppol");
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

    public static void setKeystoreLocation(String keystoreLocation) {
        KeystoreManager.keystoreLocation = keystoreLocation;
    }

    public static void setKeystorePassword(String keystorePassword) {
        KeystoreManager.keystorePassword = keystorePassword;
    }
}
