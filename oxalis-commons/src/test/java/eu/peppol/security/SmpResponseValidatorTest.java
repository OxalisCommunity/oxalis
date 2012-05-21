/* Created by steinar on 14.05.12 at 00:21 */
package eu.peppol.security;

import eu.peppol.security.SmpResponseValidator;
import eu.peppol.start.identifier.KeystoreManager;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class SmpResponseValidatorTest {


    private Document document;

    @BeforeClass
    public void loadSampleSmpResponse() throws IOException, SAXException, ParserConfigurationException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sr-smp-result.xml");
        assertNotNull(is);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        document = documentBuilder.parse(is);
    }


    @Test
    public void testVerificationOfSmpResponseSignature() throws ParserConfigurationException, IOException, SAXException {

        SmpResponseValidator smpResponseValidator = new SmpResponseValidator(document);
        boolean isValid = smpResponseValidator.isSmpSignatureValid();

        assertTrue(isValid,"Sample SMP response contained invalid signature");
    }

    @Test
    public void testRetrievalOfCertificateFromSmpResponse(){
        SmpResponseValidator smpResponseValidator = new SmpResponseValidator(document);
        X509Certificate x509Certificate = smpResponseValidator.getCertificate();
        assertNotNull(x509Certificate);
    }

    @Test
    public void testValidityOfSmpCertificate() {
        SmpResponseValidator smpResponseValidator = new SmpResponseValidator(document);
        X509Certificate smpX509Certificate = smpResponseValidator.getCertificate();

        KeystoreManager keystoreManager = new KeystoreManager();
        boolean isValid = keystoreManager.validate(smpX509Certificate);

    }

}
