package no.difi.oxalis.test.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertificateMock {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static X509Certificate withCN(String cn) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(1024, new SecureRandom()); // No ment to be secure!


            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
            X500Principal dnName = new X500Principal(String.format("CN=%s, C=NO", cn));

            certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
            certGen.setSubjectDN(dnName);
            certGen.setIssuerDN(dnName); // use the same
            certGen.setNotBefore(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
            certGen.setNotAfter(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
            certGen.setPublicKey(keyPair.getPublic());
            certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

            return certGen.generate(keyPair.getPrivate(), "BC");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
