package eu.peppol.mime;

import com.sun.xml.messaging.saaj.util.ByteOutputStream;
import eu.peppol.util.GlobalConfiguration;
import org.testng.annotations.Test;
import sun.misc.BASE64Encoder;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 19.06.13
 *         Time: 00:23
 */
@Test(groups = "integration")
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
                "\r\n" +
                "Hello world\r\n";

        PrivateKey privateKey = getPrivateKey();

        // Get a SHA1 message digest
        Signature signature = Signature.getInstance("SHA1WithRSA");
        signature.initSign(privateKey);
        byte[] dataToSign = text.getBytes("UTF-8");
        signature.update(dataToSign);
        byte[] signatureBytes = signature.sign();

        AlgorithmId digestAlgorithmId = new AlgorithmId(AlgorithmId.SHA_oid);
        AlgorithmId encryptionAlgorithm = new AlgorithmId(AlgorithmId.RSA_oid);

        X509Certificate x509Certificate = getOurCertificate();
        X500Name x500Name = X500Name.asX500Name(x509Certificate.getSubjectX500Principal());

        SignerInfo signerInfo = new SignerInfo(x500Name, x509Certificate.getSerialNumber(), digestAlgorithmId, encryptionAlgorithm, signatureBytes);
        ContentInfo contentInfo = new ContentInfo(ContentInfo.DIGESTED_DATA_OID, new DerValue(DerValue.tag_OctetString, dataToSign));

        PKCS7 pkcs7 = new PKCS7(new AlgorithmId[]{digestAlgorithmId}, contentInfo, new X509Certificate[]{}, new SignerInfo[]{signerInfo});
        ByteArrayOutputStream derOutputStream = new DerOutputStream();
        pkcs7.encodeSignedData(derOutputStream);
        byte[] encoded = derOutputStream.toByteArray();

        BASE64Encoder base64Encoder = new BASE64Encoder();
        String base64Encoded = base64Encoder.encode(encoded);
        System.out.println(base64Encoded);

        ByteOutputStream bos = new ByteOutputStream();
        mimeMultipart.writeTo(bos);
        String s2 = new String(bos.getBytes(), "UTF-8");
        assertTrue(s2.contains("text/plain"));
    }

    private PrivateKey getPrivateKey() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = loadKeystore();
        String alias = keyStore.aliases().nextElement();
        String keyStorePassword = GlobalConfiguration.getInstance().getKeyStorePassword();
        return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
    }

    private X509Certificate getOurCertificate() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        KeyStore keyStore = loadKeystore();
        String alias = keyStore.aliases().nextElement();
        return (X509Certificate) keyStore.getCertificate(alias);
    }

    private KeyStore loadKeystore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        File oxalisHomeDirectory = GlobalConfiguration.getInstance().getOxalisHomeDir();
        File f = new File(oxalisHomeDirectory, "oxalis-pilot-ap-2015.jks");
        keyStore.load(new FileInputStream(f), GlobalConfiguration.getInstance().getKeyStorePassword().toCharArray());
        return keyStore;
    }

}
