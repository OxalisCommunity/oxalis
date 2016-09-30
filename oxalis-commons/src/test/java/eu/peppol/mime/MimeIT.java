/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.mime;

import com.google.inject.Inject;
import eu.peppol.security.KeystoreManager;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OxalisCommonsModule;
import org.testng.annotations.Guice;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
@Guice(modules = {OxalisCommonsModule.class})
public class MimeIT {

    @Inject
    GlobalConfiguration globalConfiguration;
    @Inject
    KeystoreManager keystoreManager;

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

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mimeMultipart.writeTo(bos);
        String s2 = new String(bos.toByteArray(), "UTF-8");
        assertTrue(s2.contains("text/plain"));
    }

    private PrivateKey getPrivateKey() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = loadKeystore();
        String alias = keyStore.aliases().nextElement();
        String keyStorePassword = globalConfiguration.getKeyStorePassword();
        return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
    }

    private X509Certificate getOurCertificate() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        KeyStore keyStore = loadKeystore();
        String alias = keyStore.aliases().nextElement();
        return (X509Certificate) keyStore.getCertificate(alias);
    }

    private KeyStore loadKeystore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ourKeystore = keystoreManager.getOurKeystore();
        return ourKeystore;
    }

}
