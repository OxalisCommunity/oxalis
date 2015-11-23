package eu.peppol.as2;

import eu.peppol.MessageDigestResult;
import eu.peppol.security.KeystoreManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 09.10.13
 *         Time: 15:14
 */
@Test(groups = "integration")
public class MdnMimeMessageFactoryTest {

    private MdnData mdnData;
    private MdnMimeMessageFactory mdnMimeMessageFactory;

    @BeforeMethod
    public void setUp() throws Exception {
        MdnData.Builder builder = new MdnData.Builder();
        mdnData = builder.subject("Sample MDN")
                .as2From("AP_000001")
                .as2To("AP_000002")
                .disposition(As2Disposition.failed("Unknown recipient"))
                .date(new Date())
                .mic(new Mic("eeWNkOTx7yJYr2EW8CR85I7QJQY=", "sha1"))
                .build();
        mdnMimeMessageFactory = new MdnMimeMessageFactory(KeystoreManager.getInstance().getOurCertificate(), KeystoreManager.getInstance().getOurPrivateKey());
    }

    @Test
    public void testCreateMdn() throws Exception {

        MimeMessage mimeMessage = mdnMimeMessageFactory.createSignedMdn(mdnData, new InternetHeaders());
        mimeMessage.writeTo(System.out);
    }

    @Test
    public void testWithPayloadDigest() throws IOException, MessagingException {
        MdnData.Builder b = new MdnData.Builder();
        MdnData data = b.subject("MDN with PayloadDigest")
                .as2From("AP_00003")
                .as2To("AP_00004")
                .disposition(As2Disposition.processed())
                .date(new Date())
                .mic(new Mic("eeWNkOTx7yJYr2EW8CR85I7QJQY=", "sha1"))
                .originalPayloadDigest(new MessageDigestResult("XXXXXXXXX".getBytes(), "SHA-256"))
                .build();
        MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(data, new InternetHeaders());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        signedMdn.writeTo(os);
        String s = new String(os.toString("UTF-8"));

        System.out.println(s);
        assertTrue(s.contains(MdnMimeMessageFactory.X_ORIGINAL_MESSAGE_ALG), MdnMimeMessageFactory.X_ORIGINAL_MESSAGE_ALG + " not found in message");
        assertTrue(s.contains(MdnMimeMessageFactory.X_ORIGINAL_MESSAGE_DIGEST));

    }

    @Test
    public void dumpMdnAsText() throws Exception {
        MimeMessage mimeMessage = mdnMimeMessageFactory.createSignedMdn(mdnData, new InternetHeaders());

        String mdnAsText = mdnMimeMessageFactory.toString(mimeMessage);
        assertTrue(mdnAsText.contains("Unknown recipient"))   ;
    }

    @Test
    public void verifyContentsOfHumanReadablePart() throws Exception {
        MimeMessage mimeMessage = mdnMimeMessageFactory.createSignedMdn(mdnData, new InternetHeaders());

        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);

        // Outermost multipart/signed
        MimeMultipart multiPartSigned = mdnMimeMessageInspector.getSignedMultiPart();
        assertTrue(multiPartSigned.getContentType().contains("multipart/signed"));

        // First body part in multipart/report contains the plain text
        BodyPart textPlainBodyPart = mdnMimeMessageInspector.getPlainTextBodyPart();

        String plainText = mdnMimeMessageInspector.getPlainTextPartAsText();

        String errorMessage = mdnData.getAs2Disposition().getDispositionModifier().getDispositionModifierExtension();
        assertTrue(plainText.contains(errorMessage), "Invalid contents: " + plainText + ". <<< Expected error message: " + errorMessage);
    }

    @Test
    public void verifyTextOfHumanReadablePartWhenProcessingError() throws Exception {
        MdnData.Builder builder = new MdnData.Builder();
        String errorMessage = "AS2-To header equals AS2-From header";

        mdnData = builder.subject("Sample MDN")
                .as2From("AP_000001")
                .as2To("AP_000002")
                .disposition(As2Disposition.processedWithError(errorMessage))
                .date(new Date())
                .mic(new Mic("eeWNkOTx7yJYr2EW8CR85I7QJQY=", "sha1"))
                .build();
        MimeMessage mdn = mdnMimeMessageFactory.createSignedMdn(mdnData, new InternetHeaders());
        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mdn);
        assertTrue(mdnMimeMessageInspector.getPlainTextPartAsText().contains(errorMessage), "The plain text does not contain '" + errorMessage + "'");
    }

}
