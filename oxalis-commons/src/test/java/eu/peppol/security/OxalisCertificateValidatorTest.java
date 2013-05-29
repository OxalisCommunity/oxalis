package eu.peppol.security;

import eu.peppol.start.identifier.KeystoreManager;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 27.05.13
 *         Time: 12:55
 */
public class OxalisCertificateValidatorTest {

    @Test(groups = "integration")
    public void validateOurCertificate() throws Exception {

        long start = System.currentTimeMillis();
        KeystoreManager keystoreManager = KeystoreManager.getInstance();
        long end = System.currentTimeMillis();

        boolean validates = OxalisCertificateValidator.INSTANCE.validate(keystoreManager.getOurCertificate());
        assertTrue(validates);

        long complete = System.currentTimeMillis();

        long initElapsed = end - start;
        long validationElapsed = complete - end;
        System.out.printf("Init: %d, validation: %d, diff: %d\n", initElapsed, validationElapsed, validationElapsed-initElapsed);
    }
}
