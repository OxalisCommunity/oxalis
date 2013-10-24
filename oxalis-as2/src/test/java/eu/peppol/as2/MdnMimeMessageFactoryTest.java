package eu.peppol.as2;

import eu.peppol.security.KeystoreManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.mail.internet.MimeMessage;
import java.util.Date;

import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 09.10.13
 *         Time: 15:14
 */
public class MdnMimeMessageFactoryTest {

    private MdnData mdnData;

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
    }

    @Test
    public void testCreateMdn() throws Exception {


        MdnMimeMessageFactory mdnMimeMessageFactory = new MdnMimeMessageFactory(KeystoreManager.getInstance().getOurCertificate(), KeystoreManager.getInstance().getOurPrivateKey());

        MimeMessage mimeMessage = mdnMimeMessageFactory.createMdn(mdnData);
        mimeMessage.writeTo(System.out);
    }

    @Test
    public void dumpMdnAsText() throws Exception {
        MdnMimeMessageFactory mdnMimeMessageFactory = new MdnMimeMessageFactory(KeystoreManager.getInstance().getOurCertificate(), KeystoreManager.getInstance().getOurPrivateKey());
        MimeMessage mimeMessage = mdnMimeMessageFactory.createMdn(mdnData);

        String mdnAsText = mdnMimeMessageFactory.toString(mimeMessage);
        assertTrue(mdnAsText.contains("Unknown recipient"))   ;
    }
}
