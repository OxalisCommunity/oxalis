package eu.peppol.as2.servlet;

import com.google.inject.Inject;
import eu.peppol.MessageDigestResult;
import eu.peppol.as2.*;
import eu.peppol.security.KeystoreManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.security.MessageDigest;

import static org.testng.Assert.assertNotNull;

/**
 * Created by soc on 18.01.2016.
 */
@Guice(modules = {As2TestModule.class})
public class ResponseDataTest {


    @Inject
    TestDataGenerator testDataGenerator;

    @Inject
    KeystoreManager keystoreManager;

    MdnMimeMessageFactory mdnMimeMessageFactory;

    @BeforeMethod
    public void setUp() {
        mdnMimeMessageFactory = new MdnMimeMessageFactory(keystoreManager.getOurCertificate(), keystoreManager.getOurPrivateKey());
    }

    @Test()
    public void createResponsData() throws Exception {

        assertNotNull(testDataGenerator);
        InternetHeaders sampleInternetHeaders = testDataGenerator.createSampleInternetHeaders();
        InputStream inputStream = testDataGenerator.loadSbdhAsicXml();

        Mic mic = new Mic("jablajabla", "SHA-1");

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update("The quick brown fox jumped over the lazy dog".getBytes());
        MessageDigestResult messageDigestResult = new MessageDigestResult(digest.digest(), digest.getAlgorithm());

        MdnData mdnData = MdnData.Builder.buildProcessedOK(sampleInternetHeaders, mic, messageDigestResult);

        MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(mdnData, sampleInternetHeaders);
        ResponseData responseData = new ResponseData(200, signedMdn, mdnData);
    }
}