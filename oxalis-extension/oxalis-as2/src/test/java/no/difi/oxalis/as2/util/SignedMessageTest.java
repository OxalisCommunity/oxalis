package no.difi.oxalis.as2.util;

import com.google.inject.Injector;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import no.difi.vefa.peppol.common.code.Service;
import no.difi.vefa.peppol.security.api.CertificateValidator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.security.cert.X509Certificate;

/**
 * @author erlend
 */
public class SignedMessageTest {

    private X509Certificate certificate;

    private CertificateValidator certificateValidator;

    @BeforeClass
    public void beforeClass() {
        Injector injector = GuiceModuleLoader.initiate();
        certificate = injector.getInstance(X509Certificate.class);
        certificateValidator = injector.getInstance(CertificateValidator.class);
    }

    @Test
    public void oxalisSha1() throws Exception {
        SignedMessage signedMessage;

        try (InputStream inputStream = getClass().getResourceAsStream("/as2-message/oxalis-sha1.txt")) {
            signedMessage = SignedMessage.load(inputStream);
        }

        signedMessage.validate(Service.AP, certificateValidator, "APP_0000000001");
        signedMessage.validate(certificate);
    }

    @Test
    public void oxalisSha512() throws Exception {
        SignedMessage signedMessage;

        try (InputStream inputStream = getClass().getResourceAsStream("/as2-message/oxalis-sha512.txt")) {
            signedMessage = SignedMessage.load(inputStream);
        }

        signedMessage.validate(Service.AP, certificateValidator);
        signedMessage.validate(certificate);
    }

    @Test
    public void some() throws Exception {
        SignedMessage signedMessage;

        try (InputStream inputStream = getClass().getResourceAsStream("/as2-message/some.txt")) {
            signedMessage = SignedMessage.load(inputStream);
        }

        signedMessage.validate(Service.AP, certificateValidator, "APP_1000000302");
        signedMessage.validate(signedMessage.getSigner());
    }
}
