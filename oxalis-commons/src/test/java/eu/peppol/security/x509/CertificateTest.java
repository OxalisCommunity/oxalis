package eu.peppol.security.x509;

import org.testng.annotations.Test;

import java.io.InputStream;
import java.security.cert.*;
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

    @Test
    public void loadCertificatesInKeyStores() throws Exception {



    }

}
