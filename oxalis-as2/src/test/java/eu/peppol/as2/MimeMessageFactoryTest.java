package eu.peppol.as2;

import eu.peppol.security.KeystoreManager;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.mail.smime.SMIMESignedParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 08.10.13
 *         Time: 11:34
 */
public class MimeMessageFactoryTest {

    @Test
    public void testCreateSignedMimeMessage() throws Exception {

        MimeMessageFactory mimeMessageFactory = new MimeMessageFactory(KeystoreManager.getInstance().getOurPrivateKey(), KeystoreManager.getInstance().getOurCertificate());

        // Fetch input stream for data
        InputStream resourceAsStream = MimeMessageFactory.class.getClassLoader().getResourceAsStream("example.xml");
        assertNotNull(resourceAsStream);

        // Creates the signed message
        MimeMessage signedMimeMessage = mimeMessageFactory.createSignedMimeMessage(resourceAsStream, new MimeType("application","xml"));
        assertNotNull(signedMimeMessage);

        MimeMessageInspector mimeMessageInspector = new MimeMessageInspector(signedMimeMessage);
    }

    @Test
    public void inspectSignedMessage() throws Exception {
        MimeMessageFactory mimeMessageFactory = new MimeMessageFactory(KeystoreManager.getInstance().getOurPrivateKey(), KeystoreManager.getInstance().getOurCertificate());

        // Fetch input stream for data
        InputStream resourceAsStream = MimeMessageFactory.class.getClassLoader().getResourceAsStream("example.xml");
        assertNotNull(resourceAsStream);

        // Creates the signed message
        MimeMessage signedMimeMessage = mimeMessageFactory.createSignedMimeMessage(resourceAsStream, new MimeType("application","xml"));
        assertNotNull(signedMimeMessage);

        assertTrue(signedMimeMessage.getContent() instanceof MimeMultipart,"Not a MultiPart");

        // Converts the contents into a Mime Multi Part, which should consist of two body parts
        MimeMultipart mimeMultipart = (MimeMultipart) signedMimeMessage.getContent();

        // First part contains the payload
        BodyPart bodyPart = mimeMultipart.getBodyPart(0);
        // For which the contents is an input stream giving access to the actual data
        Object content = bodyPart.getContent();
        assertTrue(content instanceof InputStream);

        StringWriter sw = new StringWriter();
        int c;
        InputStream inputStream = bodyPart.getInputStream();
        while ((c= inputStream.read()) >= 0 ) {
            sw.write(c);
        }

        assertTrue(sw.toString().contains("<?xml version"));
    }
}
