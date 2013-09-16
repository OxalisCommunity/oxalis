package eu.peppol.security;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyPair;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 06.05.13
 *         Time: 22:34
 */
public class OxalisCipherTest {

    private OxalisCipher oxalisCipher;
    private OxalisCipherConverter oxalisCipherConverter;

    @BeforeMethod
    public void setUp() {
        oxalisCipher = new OxalisCipher();
        oxalisCipherConverter = new OxalisCipherConverter();

    }

    /**
     * Mimics how a servlet will respond with an encrypted entity  for which the key is encrypted and placed
     * in a header.
     */
    @Test
    public void testEncryptToStreamAndDecrypt() throws IOException {

        String plainText = "Hello World!";

        byte[] encryptedBytes = encryptString(plainText);

        String s = decryptToString(oxalisCipher, encryptedBytes);

        assertEquals(s, plainText);
    }

    /**
     * Decrypts bytes using the symmetric key held in the OxalisCipher instance.
     * Uses Cipher streams.
     *
     * @param cipherFromWrappedHexKey
     * @param encryptedBytes
     * @return
     * @throws IOException
     */
    private String decryptToString(OxalisCipher cipherFromWrappedHexKey, byte[] encryptedBytes) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encryptedBytes);

        InputStream is = oxalisCipher.decryptStream(byteArrayInputStream);

        byte[] decryptedBytes = new byte[encryptedBytes.length];

        int numberOfBytesRead = is.read(decryptedBytes);

        return new String(decryptedBytes, 0, numberOfBytesRead, Charset.forName("UTF-8"));
    }

    private byte[] encryptString(String plainText) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


        OutputStream os = oxalisCipher.encryptStream(byteArrayOutputStream);
        os.write(plainText.getBytes(Charset.forName("UTF-8")));
        os.close();

        return byteArrayOutputStream.toByteArray();
    }

    @Test(groups = {"integration"}, dataProvider = "keypair", enabled=false)
    public void encryptDataEncryptKeyAndDecrypt(KeyPair keyPair) throws Exception {
        String plainText = "Sample data for testing purposes æøå";

        byte[] encryptedBytes = encryptString(plainText);


        String encodedSymmetricKey = oxalisCipherConverter.getWrappedSymmetricKeyAsString(keyPair.getPublic(), oxalisCipher);
        assertNotNull(encodedSymmetricKey);

        OxalisCipher cipherFromWrappedHexKey = oxalisCipherConverter.createCipherFromWrappedHexKey(encodedSymmetricKey, keyPair.getPrivate());

        String decryptedResult = decryptToString(cipherFromWrappedHexKey, encryptedBytes);
    }

    @Test(groups = {"integration"}, dataProvider = "keypair", enabled=false)
    public void encryptDataWrapKeyAndDecrypt(KeyPair keyPair) throws Exception {
        String plainText = "Sample data for testing purposes æøå";

        byte[] encryptedBytes = encryptString(plainText);

        String encodedSymmetricKey = oxalisCipherConverter.getWrappedSymmetricKeyAsString(keyPair.getPublic(), oxalisCipher);
        assertNotNull(encodedSymmetricKey);

        OxalisCipher cipherFromWrappedHexKey = oxalisCipherConverter.createCipherFromWrappedHexKey(encodedSymmetricKey, keyPair.getPrivate());

        String decryptedResult = decryptToString(cipherFromWrappedHexKey, encryptedBytes);
    }

    @DataProvider(name = "keypair")
    public Object [][] createKeyPair() {

        KeyPair keyPair = new StatisticsKeyTool().loadKeyPair();
        return new Object[][] {
                { keyPair }
        };
    }
}
