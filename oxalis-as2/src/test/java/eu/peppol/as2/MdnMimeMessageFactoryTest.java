package eu.peppol.as2;

import eu.peppol.security.KeystoreManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.mail.BodyPart;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;

import static org.testng.Assert.assertEquals;
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

        MimeMessage mimeMessage = mdnMimeMessageFactory.createMdn(mdnData, new InternetHeaders());
        mimeMessage.writeTo(System.out);
    }

    @Test
    public void dumpMdnAsText() throws Exception {
        MimeMessage mimeMessage = mdnMimeMessageFactory.createMdn(mdnData, new InternetHeaders());

        String mdnAsText = mdnMimeMessageFactory.toString(mimeMessage);
        assertTrue(mdnAsText.contains("Unknown recipient"))   ;
    }

    @Test
    public void verifyContentsOfHumanReadablePart() throws Exception {
        MimeMessage mimeMessage = mdnMimeMessageFactory.createMdn(mdnData, new InternetHeaders());

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
        MimeMessage mdn = mdnMimeMessageFactory.createMdn(mdnData, new InternetHeaders());
        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mdn);
        assertTrue(mdnMimeMessageInspector.getPlainTextPartAsText().contains(errorMessage), "The plain text does not contain '" + errorMessage + "'");
    }

}
