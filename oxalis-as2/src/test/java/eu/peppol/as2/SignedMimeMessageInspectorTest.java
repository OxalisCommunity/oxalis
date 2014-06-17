package eu.peppol.as2;

import eu.peppol.security.KeystoreManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.internet.MimeMessage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 22.10.13
 *         Time: 16:13
 */
@Test(groups = "integration")
public class SignedMimeMessageInspectorTest {

    private MimeMessage signedMimeMessage;

    @BeforeMethod
    public void setUp() throws MimeTypeParseException {
        KeystoreManager keystoreManager = KeystoreManager.getInstance();
        SMimeMessageFactory sMimeMessageFactory = new SMimeMessageFactory(keystoreManager.getOurPrivateKey(), keystoreManager.getOurCertificate());
        signedMimeMessage = sMimeMessageFactory.createSignedMimeMessage("Arne Barne Busemann", new MimeType("text", "plain"));
    }

    @Test
    public void testCalculateMic() throws Exception {
        SignedMimeMessageInspector signedMimeMessageInspector = new SignedMimeMessageInspector(signedMimeMessage);
        Mic mic1 = signedMimeMessageInspector.calculateMic("sha1");
        assertNotNull(mic1);
        assertEquals(mic1.toString(), "Oqq8RQc3ff0SXMBXqh4fIwM8xGg=, sha1");
    }

}
