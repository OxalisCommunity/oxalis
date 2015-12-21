/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

/* Created by steinar on 14.05.12 at 00:21 */
package eu.peppol.security;

import eu.peppol.util.OxalisCommonsModule;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;

import static org.testng.Assert.*;

/**
 * Verifies that we parse the response from the SMP as expected.
 * <p>This test might fail if the SMP certificate has changed. In which case you must follow the steps below.</p>
 *
 * <p>
 * The prerequisite is to download two sample SMP responses from ELMA and place them into <code>sr-smp-result.xml</code> and <code>sr-utf8.xml</code>.
 * The first response should not contain national characters, while the second <em>should</em> contain national characters.
 * <p/>
 *
 * <p>The following is a suitable url to download the sample SMP response:
 * <a href="http://b-ddc207601e442e1b751e5655d39371cd.iso6523-actorid-upis.edelivery.tech.ec.europa.eu/iso6523-actorid-upis%3A%3A9908%3A810017902/services/busdox-docid-qns%3A%3Aurn%3Aoasis%3Anames%3Aspecification%3Aubl%3Aschema%3Axsd%3AInvoice-2%3A%3AInvoice%23%23urn%3Awww.cenbii.eu%3Atransaction%3Abiicoretrdm010%3Aver1.0%3A%23urn%3Awww.peppol.eu%3Abis%3Apeppol4a%3Aver1.0%3A%3A2.0">SR test url</a>
 * <p/>
 *
 * <p>
 * This is how to do it on Mac OS X or any other Linux machine with a decent set of command line programs:
 * <ol>
 *      <li>Download a SMP response without national characters:
 *          <ol>
 *              <li>Log on to <a href="https://difi.alfa1lab.com">ELMA</a></li>
 *              <li>Inspect the "Tjenestebeskrivelse" on the page "Teknisk informasjon" to ensure there are no national characters</li>
 *              <li>Save the changes into <code>sr-smp-result.xml</code> by using the <i>curl</i> command or any other command of your choice:
 *                  <pre>
 * curl http://b-ddc207601e442e1b751e5655d39371cd.iso6523-actorid-upis.edelivery.tech.ec.europa.eu/iso6523-actorid-upis%3A%3A9908%3A810017902/services/busdox-docid-qns%3A%3Aurn%3Aoasis%3Anames%3Aspecification%3Aubl%3Aschema%3Axsd%3AInvoice-2%3A%3AInvoice%23%23urn%3Awww.cenbii.eu%3Atransaction%3Abiicoretrdm010%3Aver1.0%3A%23urn%3Awww.peppol.eu%3Abis%3Apeppol4a%3Aver1.0%3A%3A2.0 -o sr-smp-result.xml
 *                  </pre>
 *              </li>
 *          </ol>
 *      </li>
 *      <li>Download a SMP response with national characters:
 *          <ol>
 *              <li>Log on to <a href="https://difi.alfa1lab.com">ELMA</a></li>
 *              <li>Modify the "Tjenestebeskrivelse" on the page "Teknisk informasjon" to include some national characters.</li>
 *              <li>Save the changes into <code>sr-utf8.xml</code>, as you did with the ordinary SMP response from above.</li>
 *              <li>Remove the national characters from the ELMA entry and save your changes</li>
 *          </ol>
 *      </li>
 * </ol>
 * <p>
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
@Guice(modules={OxalisCommonsModule.class})
public class SmpResponseValidatorTest {


    private Document document;

    @BeforeClass
    public void loadSampleSmpResponse() throws IOException, SAXException, ParserConfigurationException {
        String sendRegningSmpResponse = "sr-smp-result.xml";

        this.document = fetchAndParseSmpResponseFromClassPath(sendRegningSmpResponse);
    }

    private Document fetchAndParseSmpResponseFromClassPath(String sendRegningSmpResponse) throws ParserConfigurationException, SAXException, IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(sendRegningSmpResponse);
        assertNotNull(is);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        // Prevents XML entity expansion attacks
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document document = documentBuilder.parse(is);
        return document;
    }


    @Test(enabled = false)
    public void testVerificationOfSmpResponseSignature() throws ParserConfigurationException, IOException, SAXException {
        // TODO Currently failing
        SmpResponseValidator smpResponseValidator = new SmpResponseValidator(document);
        boolean isValid = smpResponseValidator.isSmpSignatureValid();

        assertTrue(isValid, "Sample SMP response contained invalid signature");
    }

    /**
     * Signature fails validation.
     * If you need to update this use wget to download the untouched SMP response.
     * If you use browser -> view source -> copy/paste etc, you'll most probably end up with extra line shifts
     * that break the signature.
     *
     * @see "SR-64053"
     */
    @Test(groups = {"integration"})
    public void verifySignatureFor971033533() throws Exception {
        Document responseDocument = fetchAndParseSmpResponseFromClassPath("smp-response-for-971033533.xml");
        SmpResponseValidator smpResponseValidator = new SmpResponseValidator(responseDocument);
        assertTrue(smpResponseValidator.isSmpSignatureValid(), "SMP response for smp-response-for-971033533.xml failed validation");
    }

    @Test
    public void testRetrievalOfCertificateFromSmpResponse() {
        SmpResponseValidator smpResponseValidator = new SmpResponseValidator(document);
        X509Certificate x509Certificate = smpResponseValidator.getCertificate();
        assertNotNull(x509Certificate);
    }


    /**
     * Verifies that SMP-response containing national characters, will fail validation of the signature due to
     * use of invalid character set.
     */
    @Test
    public void testSmpResponseWithNationalCharactersAndInvalidEncoding() throws ParserConfigurationException, IOException, SAXException {
        Document documentWithNationalChars = parseResponseWithCharset(Charset.forName("windows-1252")); // This should not work
        SmpResponseValidator smpResponseValidator = new SmpResponseValidator(documentWithNationalChars);
        assertFalse(smpResponseValidator.isSmpSignatureValid());
    }

    private Document parseResponseWithCharset(Charset charset) throws ParserConfigurationException, SAXException, IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sr-utf8.xml");
        assertNotNull(is);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        // Prevents XML entity expansion attacks
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        return documentBuilder.parse(new InputSource(new InputStreamReader(is, charset)));
    }

}
