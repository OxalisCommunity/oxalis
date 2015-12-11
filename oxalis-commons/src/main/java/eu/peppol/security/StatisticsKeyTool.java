package eu.peppol.security;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.RuntimeConfigurationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Provides various methods for generation, loading and saving private and public keys.
 *
 * @author steinar
 *         Date: 01.05.13
 *         Time: 21:17
 */
public class StatisticsKeyTool {

    public static final String ASYMMETRIC_KEY_ALGORITHM = "RSA";
    public static final String OXALIS_STATISTICS_PUBLIC_KEY = "oxalis-statistics-public.key";
    public static final String OXALIS_STATISTICS_PRIVATE_KEY = "oxalis-statistics-private.key";
    public static final int MAX_LENGTH_OF_ENCODED_KEY = 4096;

    public static final Logger log = LoggerFactory.getLogger(StatisticsKeyTool.class);
    private final GlobalConfiguration globalConfiguration;

    public static void main(String[] args) {

        Injector injector = Guice.createInjector(new RuntimeConfigurationModule());
        StatisticsKeyTool statisticsKeyTool = injector.getInstance(StatisticsKeyTool.class);

        KeyPair keyPair = statisticsKeyTool.createKeyPair();
        statisticsKeyTool.saveKeyPair(keyPair);

        System.out.println("Public key saved in " + statisticsKeyTool.getPublicKeyFile().getAbsolutePath());
        System.out.println("Private key saved in " + statisticsKeyTool.getPrivateKeyFile().getAbsolutePath());
    }


    @Inject
    public StatisticsKeyTool(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }

    public KeyPair createKeyPair() {

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_KEY_ALGORITHM);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return keyPair;

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to create key pair: " + e.getMessage(), e);
        }
    }

    public void saveKeyPair(KeyPair keyPair) {
        saveBytes(keyPair.getPublic(), getPublicKeyFile());
        // TODO: encrypt the private key, just in case
        saveBytes(keyPair.getPrivate(), getPrivateKeyFile());
    }

    /**
     * The Oxalis statistics public key is supplied as part of the distribution (of course).
     *
     * @return the statistics public key
     */
    public PublicKey loadPublicKeyFromClassPath() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(OXALIS_STATISTICS_PUBLIC_KEY);
        if (inputStream == null) {
            throw new IllegalStateException("Unable to locate file " + OXALIS_STATISTICS_PUBLIC_KEY);
        }
        try {
            byte[] bytes = loadBytesFrom(inputStream);
            log.info("Loaded public key with " + bytes.length + " bytes");
            return publicKeyFromBytes(bytes);

        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Invalid public key encoded in " + OXALIS_STATISTICS_PUBLIC_KEY, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to close input stream for " + OXALIS_STATISTICS_PUBLIC_KEY, e);
            }
        }
    }

    /**
     * Loads the Difi private key pertaining to Oxalis statistics.
     *
     * @return the Difi private key
     */
    public PrivateKey loadPrivateKeyFromOxalisHome() {
        String statisticsPrivateKeyPath = globalConfiguration.getStatisticsPrivateKeyPath();
        File file = new File(statisticsPrivateKeyPath);
        if (!file.exists() || !file.canRead()) {
            throw new IllegalArgumentException("Unable to load private key from " + statisticsPrivateKeyPath);
        }
        return loadPrivateKey(file);
    }


    public PublicKey loadPublicKey(File file) {
        try {
            byte[] encodedPublicKey = loadBytesFromFile(file);

            return publicKeyFromBytes(encodedPublicKey);

        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to create key from encoded specification in " + file.getAbsolutePath() + "; " + e.getMessage(), e);
        }

    }

    public PrivateKey loadPrivateKey(File privateKeyFile) {

        byte[] encodedPrivateKey = loadBytesFromFile(privateKeyFile);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);

        KeyFactory keyFactory = createKeyFactory();
        try {
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to create private key from encoded specification in " + privateKeyFile.getAbsolutePath() + "; " + e.getMessage(), e);
        }

    }

    public KeyPair loadKeyPair() {
        PrivateKey privateKey = loadPrivateKeyFromOxalisHome();
        PublicKey publicKey = loadPublicKeyFromClassPath();

        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        return keyPair;
    }


    File getPublicKeyFile() {
        return new File(getResourceDirectory(), OXALIS_STATISTICS_PUBLIC_KEY);
    }

    File getPrivateKeyFile() {
        return new File(getResourceDirectory(), OXALIS_STATISTICS_PRIVATE_KEY);
    }

    /**
     * Provides the name of the temporary directory into which generated keys will be stored.
     *
     * @return
     */
    File getResourceDirectory() {
        String tempDirName = System.getProperty("java.io.tmpdir");
        return new File(tempDirName);
    }

    private void saveBytes(Key key, File file) {
        byte[] encodedBytes = key.getEncoded();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(encodedBytes);
            fos.close();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to create file " + file.getAbsolutePath() + "; " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write bytes to " + file.getAbsolutePath() + "; " + e.getMessage(), e);
        }
    }


    private PublicKey publicKeyFromBytes(byte[] encodedPublicKey) throws InvalidKeySpecException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encodedPublicKey);

        KeyFactory keyFactory = createKeyFactory();
        return keyFactory.generatePublic(x509EncodedKeySpec);
    }


    private byte[] loadBytesFromFile(File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);

            return loadBytesFrom(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to open file " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read from file " + file.getAbsolutePath() + "; " + e.getMessage(), e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to close file " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    private byte[] loadBytesFrom(InputStream inputStream) {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[MAX_LENGTH_OF_ENCODED_KEY];

		try {
			while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toByteArray();

	}

    private KeyFactory createKeyFactory() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_KEY_ALGORITHM);
            return keyFactory;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to create key factory with algorithm " + ASYMMETRIC_KEY_ALGORITHM + "; " + e.getMessage(), e);
        }
    }

}
