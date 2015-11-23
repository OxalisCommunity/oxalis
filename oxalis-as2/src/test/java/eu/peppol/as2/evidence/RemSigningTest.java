/*
 * Copyright (c) 2015 Steinar Overbeck Cook
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.as2.evidence;

import eu.peppol.security.KeystoreManager;
import no.difi.vefa.peppol.security.api.PeppolSecurityException;
import no.difi.vefa.peppol.security.xmldsig.XmldsigSigner;
import no.difi.vefa.peppol.security.xmldsig.XmldsigVerifier;
import org.etsi.uri._02640.v2_.REMEvidenceType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;


/**
 * @author steinar
 *         Date: 23.11.2015
 *         Time: 21.35
 */
public class RemSigningTest {

    private KeyStore.PrivateKeyEntry privateKeyEntry;

    @BeforeClass
    public void setUp() {
        privateKeyEntry = new KeyStore.PrivateKeyEntry(KeystoreManager.getInstance().getOurPrivateKey(), new Certificate[]{KeystoreManager.getInstance().getOurCertificate()});

    }

    @Test
    public void loadSignAndFailVerification() throws Exception {

        InputStream sampleInputStream = RemSigningTest.class.getClassLoader().getResourceAsStream("sample-rem-with-mdn-fails.xml");

        assertNotNull(sampleInputStream);

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document originalDocument = documentBuilder.parse(sampleInputStream);

        JAXBContext jaxbContext = JAXBContext.newInstance(REMEvidenceType.class);


        Document signedDocument = documentBuilder.newDocument();
        DOMResult result = new DOMResult(signedDocument);
        XmldsigSigner.sign(originalDocument, privateKeyEntry, result);

        try {
            XmldsigVerifier.verify((Document) result.getNode());
            fail("This is supposed to fail");
        } catch (PeppolSecurityException e) {
        }
    }

    @Test
    public void loadSignAndVerify() throws Exception {
        InputStream sampleInputStream = RemSigningTest.class.getClassLoader().getResourceAsStream("sample-rem-with-mdn-ok.xml");

        assertNotNull(sampleInputStream);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);   // <<<< This does the trick !!
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document originalDocument = documentBuilder.parse(sampleInputStream);

        JAXBContext jaxbContext = JAXBContext.newInstance(REMEvidenceType.class);


        Document signedDocument = documentBuilder.newDocument();
        DOMResult result = new DOMResult(signedDocument);
        XmldsigSigner.sign(originalDocument, privateKeyEntry, result);

        XmldsigVerifier.verify((Document) result.getNode());
    }
}
