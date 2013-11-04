package eu.peppol.as2;

import eu.peppol.security.KeystoreManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 08.10.13
 *         Time: 11:10
 */
public class As2MessageInspectorTest {


    // Created by the setUp() method
    private As2Message as2Message;

    @BeforeMethod
    public void setUp() throws Exception {

        // We must supply our certificate as part of the signature for validation
        X509Certificate ourCertificate = KeystoreManager.getInstance().getOurCertificate();

        // Obtains our private key for the actual signature of the message
        PrivateKey ourPrivateKey = KeystoreManager.getInstance().getOurPrivateKey();

        // Fetch input stream for sample data
        InputStream resourceAsStream = As2MessageInspectorTest.class.getClassLoader().getResourceAsStream("example.xml");
        assertNotNull(resourceAsStream);

        // The content-type must be manually specified as there is no way of automatically probing the file.
        MimeType mimeType = new MimeType("application", "xml");

        // Creates the S/MIME message
        SMimeMessageFactory SMimeMessageFactory = new SMimeMessageFactory(ourPrivateKey, ourCertificate);
        MimeMessage signedMimeMessage = SMimeMessageFactory.createSignedMimeMessage(resourceAsStream, mimeType);
        assertNotNull(signedMimeMessage);

        // Finally we add the required headers
        As2Message.Builder builder = new As2Message.Builder(signedMimeMessage);

        builder.as2To(ourCertificate.getSubjectX500Principal().getName());
        builder.as2From(new As2SystemIdentifier(ourCertificate.getSubjectX500Principal()));
        builder.messageId("42");
        builder.date(new Date());
        builder.subject("PEPPOL Message");

        as2Message = builder.build();
    }

    /**
     * Validates the AS2 Message created in the set up
     *
     * @throws Exception
     */
    @Test
    public void validateAs2Message() throws Exception {

        As2MessageInspector.validate(as2Message);

    }
}
