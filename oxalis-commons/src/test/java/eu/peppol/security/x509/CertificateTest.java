package eu.peppol.security.x509;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 21.05.13
 *         Time: 16:16
 */
public class CertificateTest {

    @Test
    public void loadCertificate() throws Exception {

        InputStream inputStream = CertificateTest.class.getClassLoader().getResourceAsStream("unit4-accesspoint.cer");
        if (inputStream == null) {
            throw new IllegalStateException("Unable to find SR certificate");
        }

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        Certificate certificate = certificateFactory.generateCertificate(inputStream);
        assertNotNull(certificate,"No certificate generated");
        assertEquals(certificate.getType(), "X.509");

        X509Certificate x509Certificate = (X509Certificate) certificate;
        x509Certificate.checkValidity(new Date());

        CertPath certPath = certificateFactory.generateCertPath(Arrays.asList(x509Certificate));
        CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX");
    }

    /**
     * Creates a X509 V3 certificate using Bouncy Castle
     *
     * @throws NoSuchAlgorithmException
     * @throws OperatorCreationException
     * @throws CertificateException
     * @throws NoSuchProviderException
     */
    @Test
    public void createSampleCertificate() throws NoSuchAlgorithmException, OperatorCreationException, CertificateException, NoSuchProviderException {

        Security.addProvider(new BouncyCastleProvider());

        KeyPair keyPair = generateKeyPair();

        ContentSigner sigGen = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(BouncyCastleProvider.PROVIDER_NAME).build(keyPair.getPrivate());


        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);

        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        X509v3CertificateBuilder x509v3CertificateBuilder = new X509v3CertificateBuilder(new X500Name("CN=AP_UNIT_TEST"), BigInteger.ONE, startDate, endDate, new X500Name("CN=AP_UNIT_TEST"), subjectPublicKeyInfo);
        X509CertificateHolder x509CertificateHolder = x509v3CertificateBuilder.build(sigGen);
        X509Certificate cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(x509CertificateHolder);

    }

    protected KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();

        keyPairGenerator.initialize(2048, secureRandom);
        return keyPairGenerator.generateKeyPair();
    }
}
