package eu.peppol.as2;

import eu.peppol.security.KeystoreManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author steinar
 *         Date: 28.10.13
 *         Time: 12:08
 */
public class As2MessageTest {


    private MimeMessage signedMimeMessage;

    @BeforeMethod
    public void setUp() throws Exception {
        X509Certificate ourCertificate = KeystoreManager.getInstance().getOurCertificate();
        PrivateKey ourPrivateKey = KeystoreManager.getInstance().getOurPrivateKey();
        SmimeMessageFactory SmimeMessageFactory = new SmimeMessageFactory(ourPrivateKey, ourCertificate);

        InputStream resourceAsStream = As2MessageTest.class.getResourceAsStream("/peppol-bis-invoice-sbdh.xml");
        assertNotNull(resourceAsStream);
        signedMimeMessage = SmimeMessageFactory.createSignedMimeMessage(resourceAsStream, new MimeType("application/xml"));


    }

    @Test
    public void getPropertiesFromInvalidAs2Message() throws Exception {

        As2Message.Builder builder = new As2Message.Builder(signedMimeMessage);
        As2Message as2Message = builder.as2From((String) null)
                .as2To(null)
                .as2From((As2SystemIdentifier) null)
                .build();

        assertNull(as2Message.getAs2From());
        assertNull(as2Message.getAs2To());

    }
}
