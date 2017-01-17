package no.difi.oxalis.commons.mode;

import com.google.inject.Inject;
import no.difi.oxalis.commons.guice.TestOxalisKeystoreModule;
import no.difi.vefa.peppol.common.code.Service;
import no.difi.vefa.peppol.security.api.CertificateValidator;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.security.cert.X509Certificate;

@Guice(modules = {ModeModule.class, TestOxalisKeystoreModule.class})
public class ModeModuleTest {

    @Inject
    private CertificateValidator certificateValidator;

    @Inject
    private X509Certificate certificate;

    @Test
    public void simple() throws Exception {
        certificateValidator.validate(Service.AP, certificate);
    }
}
