package eu.peppol.mime;

import com.sun.xml.messaging.saaj.util.ByteOutputStream;
import eu.peppol.util.GlobalConfiguration;
import org.testng.annotations.Test;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 19.06.13
 *         Time: 00:23
 */
public class MimeTest {

    @Test
    public void testMimeMessage() throws Exception {

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        String content = "Hello world";
        mimeBodyPart.setText(content);

        mimeBodyPart.setHeader("Content-Type", "text/plain");
        MimeMultipart mimeMultipart = new MimeMultipart();
        mimeMultipart.setPreamble("The preamble");
        mimeMultipart.addBodyPart(mimeBodyPart);

        String text = "Content-Type: text/plain\r\n" +
                "/r\n" +
                "Hello world\r\n";
        // Get a SHA1 message digest
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        sha1.update(text.getBytes());
        byte[] digestBytes = sha1.digest();

        PrivateKey key = getPrivateKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = cipher.doFinal(digestBytes);


        BASE64Encoder base64Encoder = new BASE64Encoder();
        String encode = base64Encoder.encode(cipherText);
        System.out.println(encode);

        ByteOutputStream bos = new ByteOutputStream();

        mimeMultipart.writeTo(bos);

        String s2 = new String(bos.getBytes(), "UTF-8");

        assertTrue(s2.contains("text/plain"));
    }

    private PrivateKey getPrivateKey() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = loadKeystore();
        String alias = keyStore.aliases().nextElement();
        return (PrivateKey) keyStore.getKey(alias, "peppol".toCharArray());
    }

    private KeyStore loadKeystore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        File f = new File("/Users/steinar/.oxalis/oxalis-pilot.jks");
        keyStore.load(new FileInputStream(f), "peppol".toCharArray());
        return keyStore;

    }
}
