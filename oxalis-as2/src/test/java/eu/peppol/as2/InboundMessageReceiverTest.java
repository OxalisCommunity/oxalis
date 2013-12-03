package eu.peppol.as2;

import eu.peppol.document.SbdhMessageRepository;
import eu.peppol.document.SimpleSbdhMessageRepository;
import eu.peppol.security.KeystoreManager;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.fail;

/**
 * Simulates reception of a an AS2 Message, which is validated etc. and finally produces a MDN.
 *
 * @author steinar
 *         Date: 21.10.13
 *         Time: 21:50
 */
@Test(groups = "integration")
public class InboundMessageReceiverTest {

    private ByteArrayInputStream inputStream;
    private InternetHeaders headers;
    private SbdhMessageRepository sbdhMessageRepository;

    @BeforeMethod
    public void createHeaders() {
        headers = new InternetHeaders();
        headers.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(), "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        headers.addHeader(As2Header.AS2_TO.getHttpHeaderName(), "APP_1000000006");
        headers.addHeader(As2Header.AS2_FROM.getHttpHeaderName(), "APP_1000000006");
        headers.addHeader(As2Header.MESSAGE_ID.getHttpHeaderName(), "42");
        headers.addHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
        headers.addHeader(As2Header.SUBJECT.getHttpHeaderName(), "An AS2 message");
        headers.addHeader(As2Header.DATE.getHttpHeaderName(), "Mon Oct 21 22:01:48 CEST 2013");

        String tempDir = System.getProperty("java.io.tmpdir");
        sbdhMessageRepository = new SimpleSbdhMessageRepository(tempDir);
    }

    @BeforeMethod
    public void createInputStream() throws MimeTypeParseException, IOException, MessagingException {
        SMimeMessageFactory SMimeMessageFactory = new SMimeMessageFactory(KeystoreManager.getInstance().getOurPrivateKey(), KeystoreManager.getInstance().getOurCertificate());

        // Fetch input stream for data
        InputStream resourceAsStream = SMimeMessageFactory.class.getClassLoader().getResourceAsStream("peppol-bis-invoice-sbdh.xml");
        assertNotNull(resourceAsStream);

        // Creates the signed message
        MimeMessage signedMimeMessage = SMimeMessageFactory.createSignedMimeMessage(resourceAsStream, new MimeType("application","xml"));
        assertNotNull(signedMimeMessage);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        signedMimeMessage.writeTo(baos);

        inputStream = new ByteArrayInputStream(baos.toByteArray());

        signedMimeMessage.writeTo(System.out);
    }

    @Test
    public void loadAndReceiveTestMessageOK() throws Exception {

        InboundMessageReceiver inboundMessageReceiver = new InboundMessageReceiver();

        MdnData mdnData = inboundMessageReceiver.receive(headers, inputStream, sbdhMessageRepository);

        assertEquals(mdnData.getAs2Disposition().getDispositionType(), As2Disposition.DispositionType.PROCESSED);
        AssertJUnit.assertNotNull(mdnData.getMic());
    }

    /**
     * Specifies an invalid MIC algorithm (MD5), which should cause reception to fail.
     *
     * @throws Exception
     */
    @Test
    public void receiveMessageWithInvalidDispositionRequest() throws Exception {

        headers.setHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(), "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,md5");

        InboundMessageReceiver inboundMessageReceiver = new InboundMessageReceiver();

        MdnData mdnData = null;
        try {
            mdnData = inboundMessageReceiver.receive(headers, inputStream, sbdhMessageRepository);
            fail("Reception of AS2 messages request MD5 as the MIC algorithm, should have failed");
        } catch (ErrorWithMdnException e) {
            assertNotNull(e.getMdnData(), "MDN should have been returned upon reception of invalid AS2 Message");
            assertEquals(e.getMdnData().getAs2Disposition().getDispositionType(), As2Disposition.DispositionType.FAILED);
            assertEquals(e.getMdnData().getSubject(), MdnData.SUBJECT);
        }
    }
}
