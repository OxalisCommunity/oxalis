package eu.peppol.as2;

import eu.peppol.security.KeystoreManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 08.10.13
 *         Time: 11:10
 */
public class As2MessageInspectorTest {


    private As2Message as2Message;

    @BeforeMethod
    public void setUp() throws Exception {

        X509Certificate ourCertificate = KeystoreManager.getInstance().getOurCertificate();
        MimeMessageFactory mimeMessageFactory = new MimeMessageFactory(KeystoreManager.getInstance().getOurPrivateKey(), ourCertificate);

        // Fetch input stream for data
        InputStream resourceAsStream = MimeMessageFactory.class.getClassLoader().getResourceAsStream("example.xml");
        assertNotNull(resourceAsStream);

        // Creates the signed message
        MimeType mimeType = new MimeType("application", "xml");
        String s = mimeType.toString();

        MimeMessage signedMimeMessage = mimeMessageFactory.createSignedMimeMessage(resourceAsStream, mimeType);
        assertNotNull(signedMimeMessage);

        As2Message.Builder builder = new As2Message.Builder(signedMimeMessage);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");


        builder.as2To(ourCertificate.getSubjectX500Principal().getName());
        builder.as2From(new As2SystemIdentifier(ourCertificate.getSubjectX500Principal()));
        builder.messageId("42");
        builder.date(simpleDateFormat.format(new Date()));
        builder.subject("PEPPOL Message");

        as2Message = builder.build();
    }

    @Test
    public void validateAs2Message() throws Exception {

        As2MessageInspector.validate(as2Message);


    }
}
