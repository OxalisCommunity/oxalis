package eu.peppol.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Various generic methods useful for working with Java key stores.
 *
 * @author steinar
 *         Date: 08.08.13
 *         Time: 09:30
 */
public class KeyStoreUtil {

    public static final Logger log = LoggerFactory.getLogger(KeyStoreUtil.class);

    /**
     * Loads a JKS keystore according to the parameters supplied.
     *
     * @param location physical location, i.e. file name of JKS keystore
     * @param password password of keystore file.
     * @return
     */
    public static KeyStore loadJksKeystore(String location, String password) {

        File keyStoreFile = new File(location);

        return loadJksKeystore(keyStoreFile, password);
    }

    public static KeyStore loadJksKeystore(File keyStoreFile, String password) {
        try {
            FileInputStream inputStream = new FileInputStream(keyStoreFile);

            return loadJksKeystoreAndCloseStream(inputStream, password);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to open keystore " + keyStoreFile.getAbsolutePath(), e);
        }

    }

    /**
     * Convenience method for loading a JKS keystore.
     *
     * @param inputStream
     * @param password
     * @return
     */
    public static KeyStore loadJksKeystoreAndCloseStream(InputStream inputStream, String password) {
        try {

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
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

    public static KeyStore loadTrustStore(String trustStoreResourceName, String password) {

        InputStream inputStream = KeyStoreUtil.class.getClassLoader().getResourceAsStream(trustStoreResourceName);
        if (inputStream == null) {
            throw new IllegalStateException("Unable to load trust store resource " + trustStoreResourceName + " from class path");
        }

        return loadJksKeystoreAndCloseStream(inputStream, password);
    }

    public static X509Certificate getFirstCertificate(KeyStore keyStore) {
        String alias = null;
        try {
            alias = keyStore.aliases().nextElement();
            return (X509Certificate) keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Unable to iterate the entries in the keystore " + e.getMessage(), e);
        }
    }

    /**
     * Combines the entries of several key stores into a single key store.
     *
     * @param trustStores
     * @return
     */
    public static KeyStore combineTrustStores(KeyStore... trustStores) {
        KeyStore combinedTrustStore = null;
        try {
            combinedTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            combinedTrustStore.load(null);
            int trustStoreIndex = 0;
            for (KeyStore trustStore : trustStores) {
                Enumeration<String> aliases = trustStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    // Ensures that each alias is unique in order to prevent entries from being overwritten.
                    String newAlias = alias + "-" + trustStoreIndex;
                    if (trustStore.isCertificateEntry(alias)) {
                        X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
                        log.debug("Adding alias {} for certificate {}", newAlias, certificate.getSubjectDN().getName());

                        combinedTrustStore.setCertificateEntry(newAlias, certificate);
                    }
                }
                trustStoreIndex++;
            }

        } catch (Exception e) {
            throw new IllegalStateException("Error while combining trust stores " + e.getMessage(), e);
        }

        return combinedTrustStore;

    }

    /**
     * Loads a list of key stores specified by the supplied list of resource names
     */
    public static List<KeyStore> loadKeyStores(List<String> resourceNames, String password) {

        List<KeyStore> loadedKeystores = new ArrayList<KeyStore>();
        for (String resourceName : resourceNames) {
            KeyStore keyStore = KeyStoreUtil.loadTrustStore(resourceName, password);
            loadedKeystores.add(keyStore);
        }
        return loadedKeystores;
    }
}
