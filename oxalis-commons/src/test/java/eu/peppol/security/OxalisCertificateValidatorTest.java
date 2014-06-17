package eu.peppol.security;

import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OperationalMode;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Attempts to validate our three different PEPPOL certificates against combinations of the PEPPOL trust stores
 * supplied as part of the Oxalis distribution.
 * Will only work given a OXALIS_HOME directory containing the three different certificates referenced
 * in the {@link #setUp()} method.
 *
 * @author steinar
 *         Date: 27.05.13
 *         Time: 12:55
 */
@Test(groups = "integration")
public class OxalisCertificateValidatorTest {


    private File oxalisHomeDir;
    private X509Certificate ourVersion1Certificate;
    private KeyStore v1TrustStore;
    private KeyStore v2TestTrustStore;
    private KeyStore v2ProductionTrustStore;
    private X509Certificate ourVersion2TestCertificate;
    private X509Certificate ourVersion2ProductionCertificate;

    @BeforeTest
    public void setUp() {
        oxalisHomeDir = GlobalConfiguration.getInstance().getOxalisHomeDir();

        String keyStorePassword = GlobalConfiguration.getInstance().getKeyStorePassword();
        String trustStorePassword = GlobalConfiguration.getInstance().getTrustStorePassword();

        // Loads our three different certificates
        ourVersion1Certificate = loadOurCertificate("oxalis-keystore.jks", keyStorePassword);
        ourVersion2TestCertificate = loadOurCertificate("oxalis-pilot.jks", keyStorePassword);
        ourVersion2ProductionCertificate = loadOurCertificate("oxalis-production.jks", keyStorePassword);


        // Loads the three PEPPOL trust chains into three separate key stores
        v1TrustStore = KeyStoreUtil.loadTrustStore(PeppolTrustStore.TrustStoreResource.V1.getResourcename(), trustStorePassword);
        v2TestTrustStore = KeyStoreUtil.loadTrustStore(PeppolTrustStore.TrustStoreResource.V2_TEST.getResourcename(), trustStorePassword);
        v2ProductionTrustStore = KeyStoreUtil.loadTrustStore(PeppolTrustStore.TrustStoreResource.V2_PRODUCTION.getResourcename(), trustStorePassword);
    }

    private X509Certificate loadOurCertificate(String fileName, String password) {
        File keyStoreFile = new File(oxalisHomeDir, fileName);
        KeyStore v1Keystore = KeyStoreUtil.loadJksKeystore(keyStoreFile, password);
        return KeyStoreUtil.getFirstCertificate(v1Keystore);
    }


    /**
     * Validates our certificate against the default chain of trust as specified in the global properties file.
     *
     * @throws Exception
     */
    @Test
    public void validateOurCertificate() throws Exception {

        long start = System.currentTimeMillis();
        KeystoreManager keystoreManager = KeystoreManager.getInstance();
        long end = System.currentTimeMillis();

        boolean validates = OxalisCertificateValidator.getInstance().validate(keystoreManager.getOurCertificate());
        assertTrue(validates);

        long complete = System.currentTimeMillis();

        long initElapsed = end - start;
        long validationElapsed = complete - end;
        System.out.printf("Validation of certificate, init: %d, validation: %d, diff: %d\n", initElapsed, validationElapsed, validationElapsed - initElapsed);
    }


    /**
     * Verifies that the cache is being used when validating a certificate for the second time.
     *
     * @throws Exception
     */
    public void validateOurCertificateInCache() throws Exception {

        long start = System.currentTimeMillis();
        KeystoreManager keystoreManager = KeystoreManager.getInstance();
        long end = System.currentTimeMillis();

        OxalisCertificateValidator instance = OxalisCertificateValidator.getInstance();
        int cacheHitsBefore = instance.getCacheHits();

        boolean validates = instance.validate(keystoreManager.getOurCertificate());
        assertTrue(validates);
        validates = instance.validate(keystoreManager.getOurCertificate());
        assertTrue(validates);
        int cacheHitsAfter = instance.getCacheHits();

        assertTrue(cacheHitsAfter > cacheHitsBefore);

        long complete = System.currentTimeMillis();

        long initElapsed = end - start;
        long validationElapsed = complete - end;
        System.out.printf("Validation of certificate, init: %d, validation: %d, diff: %d\n", initElapsed, validationElapsed, validationElapsed - initElapsed);
    }

    /** Validates our V1 certificate against all three PEPPOL chains of trust. Our version 1 certificate no longer validates as it has expired */
    @Test(groups = "integration", enabled = false)
    public void validateOurV1Certificate() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, CertPathValidatorException {

/* Our V1 certificate has expired
        boolean isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion1Certificate, v1TrustStore);
        assertTrue(isValid, "V1 certificate not validated against v1TrustStore");
*/

        // The V2 test chain of trust uses the same public key as the V1 trust store, i.e. should validate
        boolean isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion1Certificate, v2TestTrustStore);
        assertTrue(isValid);

        isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion1Certificate, v2ProductionTrustStore);
        assertFalse(isValid,"V1 certificate should not validateUsingCache agains v2 production trusted certificate");
    }

    /**
     * Validates all our three certificates against the chain of trust configured with TEST mode and phase T.
     *
     * @throws Exception
     */
    @Test
    public void validateTestModeInTransitionPhase() throws Exception {

        PeppolTrustStore peppolTrustStore = new PeppolTrustStore();
        KeyStore trustStore = peppolTrustStore.loadTrustStoreFor(PkiVersion.T, OperationalMode.TEST);

        boolean isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion2ProductionCertificate, trustStore);
        assertFalse(isValid);

/*
        isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion1Certificate, trustStore);
        assertTrue(isValid);
*/

        isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion2TestCertificate, trustStore);
        assertTrue(isValid);

    }

    /**
     * Validates all our three certificates against the chain of trust configured with TEST mode and phase V2.
     *
     * @throws Exception
     */
    @Test
    public void validateTestModeInPhaseV2() throws Exception {

        PeppolTrustStore peppolTrustStore = new PeppolTrustStore();
        KeyStore trustStore = peppolTrustStore.loadTrustStoreFor(PkiVersion.V2, OperationalMode.TEST);

        boolean isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion2ProductionCertificate, trustStore);
        assertFalse(isValid);

/*
        isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion1Certificate, trustStore);
        assertTrue(isValid);
*/

        isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion2TestCertificate, trustStore);
        assertTrue(isValid);

    }


    /**
     * Validates all our three certificates against the chain of trust configured with PRODUCTION mode and phase T.
     *
     * @throws Exception
     */
    @Test
    public void validateProductionModeInTransitionPhase() throws Exception {

        PeppolTrustStore peppolTrustStore = new PeppolTrustStore();
        KeyStore trustStore = peppolTrustStore.loadTrustStoreFor(PkiVersion.T, OperationalMode.PRODUCTION);

        boolean isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion2ProductionCertificate, trustStore);
        assertTrue(isValid);

        // Our version 1 certificate has expired

//        isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion1Certificate, trustStore);
//        assertTrue(isValid);

        isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion2TestCertificate, trustStore);
        assertTrue(isValid);

    }

    /**
     * Validates all our three certificates against the chain of trust configured with PRODUCTION mode and phase V2.
     *
     * @throws Exception
     */
    @Test
    public void validateProductionModeInPhase2() throws Exception {

        PeppolTrustStore peppolTrustStore = new PeppolTrustStore();
        KeyStore trustStore = peppolTrustStore.loadTrustStoreFor(PkiVersion.V2, OperationalMode.PRODUCTION);

        boolean isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion2ProductionCertificate, trustStore);
        assertTrue(isValid);

        isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion1Certificate, trustStore);
        assertFalse(isValid);

        isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion2TestCertificate, trustStore);
        assertFalse(isValid);
    }

    /**
     * Validates all our three certificates against the chain of trust configured with PRODUCTION mode and phase V2.
     *
     * @throws Exception
     */
    @Test
    public void validateProductionModeInPhase1() throws Exception {

        PeppolTrustStore peppolTrustStore = new PeppolTrustStore();
        KeyStore trustStore = peppolTrustStore.loadTrustStoreFor(PkiVersion.V1, OperationalMode.PRODUCTION);

        boolean isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion2ProductionCertificate, trustStore);
        assertFalse(isValid);

        // Our version 1 certificate has now expired
        /*
        isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion1Certificate, trustStore);
        assertTrue(isValid);
        */

        isValid = OxalisCertificateValidator.getInstance().validateWithoutCache(ourVersion2TestCertificate, trustStore);
        assertTrue(isValid);
    }

    @Test(dataProvider = "hostsToTest", enabled = false)
    public void validateKnownStarCertificate(String url, String info) throws Exception {
        URL destinationURL = new URL(url);
        HttpsURLConnection sslConnection = (HttpsURLConnection) destinationURL.openConnection();
        sslConnection.connect();
        Certificate[] sslCertificates = sslConnection.getServerCertificates();
        System.out.printf("\nValidating SSL cert for %s (%s)\n", url, info);
        for (Certificate c : sslCertificates) {
            assertTrue(c instanceof X509Certificate);
            X509Certificate cert = (X509Certificate) c;
            System.out.println(cert.getSubjectDN().getName());
            cert.checkValidity();
        }
    }

    @DataProvider
    private static final Object[][] hostsToTest() {
        return new Object[][]{
            { "https://aksesspunkt.sendregning.no/", "Not a star cert, just an example" },
            { "https://ap.unit4.com/", "UNIT4 uses star cert" },
            { "https://www.galaxygw.com/", "GalaxyGW uses star cert" }
        };
    }

}
