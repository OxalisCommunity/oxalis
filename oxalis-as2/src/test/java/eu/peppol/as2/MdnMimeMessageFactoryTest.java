package eu.peppol.as2;

import eu.peppol.security.KeystoreManager;
import org.testng.annotations.Test;

import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * @author steinar
 *         Date: 09.10.13
 *         Time: 15:14
 */
public class MdnMimeMessageFactoryTest {


    @Test
    public void testCreateMdn() throws Exception {

        MdnData.Builder builder = new MdnData.Builder();
        MdnData mdnData = builder.subject("Sample MDN")
                .as2From(new As2SystemIdentifier("AP_000001"))
                .as2To(new As2SystemIdentifier("AP_000002"))
                .disposition(As2Disposition.failed("Unknown receipient"))
                .date(new Date())
                .mic("abcdefg")
                .build();

        MdnMimeMessageFactory mdnMimeMessageFactory = new MdnMimeMessageFactory(KeystoreManager.getInstance().getOurCertificate(), KeystoreManager.getInstance().getOurPrivateKey());


        MimeMessage mimeMessage = mdnMimeMessageFactory.createMdn(mdnData);
        mimeMessage.writeTo(System.out);


    }
}
