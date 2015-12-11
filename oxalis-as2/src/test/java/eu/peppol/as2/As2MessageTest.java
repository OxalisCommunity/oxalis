package eu.peppol.as2;

import com.google.inject.Inject;
import eu.peppol.security.KeystoreManager;
import eu.peppol.util.RuntimeConfigurationModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;

import javax.activation.MimeType;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 28.10.13
 *         Time: 12:08
 */
@Guice(modules = {RuntimeConfigurationModule.class})
public class As2MessageTest {


    private MimeMessage signedMimeMessage;
    @Inject
    KeystoreManager keystoreManager;

    @BeforeMethod
    public void setUp() throws Exception {
        X509Certificate ourCertificate = keystoreManager.getOurCertificate();
        PrivateKey ourPrivateKey = keystoreManager.getOurPrivateKey();
        SMimeMessageFactory SMimeMessageFactory = new SMimeMessageFactory(ourPrivateKey, ourCertificate);

        InputStream resourceAsStream = As2MessageTest.class.getResourceAsStream("/as2-peppol-bis-invoice-sbdh.xml");
        assertNotNull(resourceAsStream);

        signedMimeMessage = SMimeMessageFactory.createSignedMimeMessage(resourceAsStream, new MimeType("application/xml"));


    }
}
