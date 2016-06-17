/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.document;

import org.testng.annotations.Test;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.DocumentIdentification;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocument;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 23.10.13
 *         Time: 14:51
 */
public class ParseSbdhTest {

    @Test(enabled = false)
    public void testParseSbdh() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance("org.unece.cefact.namespaces.standardbusinessdocumentheader");

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        URL resource = ParseSbdhTest.class.getClassLoader().getResource("peppol-bis-invoice-sbdh.xml");
        assertNotNull(resource);

        File file = new File(resource.toURI());
        assertTrue(file.isFile() && file.canRead());

        FileInputStream fileInputStream = new FileInputStream(file);

        // Parses the entire XML document
        //noinspection unchecked
        JAXBElement<StandardBusinessDocument> sbdh = (JAXBElement) unmarshaller.unmarshal(fileInputStream);
        assertNotNull(sbdh);

        StandardBusinessDocument standardBusinessDocument = sbdh.getValue();
        DocumentIdentification documentIdentification = standardBusinessDocument.getStandardBusinessDocumentHeader().getDocumentIdentification();
        assertNotNull(documentIdentification);

        String type = standardBusinessDocument.getStandardBusinessDocumentHeader().getBusinessScope().getScope().get(0).getType();
        String instanceIdentifier = standardBusinessDocument.getStandardBusinessDocumentHeader().getBusinessScope().getScope().get(0).getInstanceIdentifier();

        System.out.println(type);
        System.out.println(instanceIdentifier);
    }


}
