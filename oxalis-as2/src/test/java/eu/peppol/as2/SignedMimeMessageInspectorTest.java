package eu.peppol.as2;

import eu.peppol.MessageDigestResult;
import eu.peppol.security.KeystoreManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 22.10.13
 *         Time: 16:13
 */
@Test(groups = "integration")
public class SignedMimeMessageInspectorTest {

    private MimeMessage signedMimeMessage;
    private SMimeMessageFactory sMimeMessageFactory;

    @BeforeMethod
    public void setUp() throws MimeTypeParseException {
        KeystoreManager keystoreManager = KeystoreManager.getInstance();
        sMimeMessageFactory = new SMimeMessageFactory(keystoreManager.getOurPrivateKey(), keystoreManager.getOurCertificate());
        signedMimeMessage = sMimeMessageFactory.createSignedMimeMessage("Arne Barne Busemann", new MimeType("text", "plain"));
    }

    @Test
    public void testCalculateMic() throws Exception {
        SignedMimeMessageInspector signedMimeMessageInspector = new SignedMimeMessageInspector(signedMimeMessage);
        Mic mic1 = signedMimeMessageInspector.calculateMic("sha1");
        assertNotNull(mic1);
        assertEquals(mic1.toString(), "Oqq8RQc3ff0SXMBXqh4fIwM8xGg=, sha1");
    }

    @Test
    public void testParseSignedMessage() throws Exception {
        SignedMimeMessageInspector signedMimeMessageInspector = new SignedMimeMessageInspector(signedMimeMessage);
        try {
            signedMimeMessageInspector.parseSignedMessage();
        } catch (Exception e){
            assertTrue(false, e.getMessage());
        }
        assertTrue(true);
    }

    @Test
    public void parseMessageWithSbdh() throws Exception {
        InputStream is = SignedMimeMessageInspectorTest.class.getClassLoader().getResourceAsStream("as2-peppol-bis-invoice-sbdh.xml");
        assertNotNull(is, "as2-peppol-bis-invoice-sbdh.xml not found in class path");
        MimeMessage signedMimeMessage = sMimeMessageFactory.createSignedMimeMessage(is, new MimeType("application/xml"));
        SignedMimeMessageInspector inspector = new SignedMimeMessageInspector(signedMimeMessage);

        MessageDigestResult messageDigestResult = inspector.calcPayloadDigest("SHA-256");
        System.out.println(messageDigestResult.getAlgorithmName() + " Digest in Base64: " + messageDigestResult.getDigestAsString());
    }

}
