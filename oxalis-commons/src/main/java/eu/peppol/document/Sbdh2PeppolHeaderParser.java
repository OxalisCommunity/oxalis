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

package eu.peppol.document;

import eu.peppol.PeppolStandardBusinessHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocument;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * Parses the SBDH into a PeppolStandardBusinessHeader instance
 *
 * @author steinar
 * @author thore
 *
 * @deprecated due to severe performance problems with large XML files, use the {@link SbdhFastParser}  combined with
 * {@link Sbdh2PeppolHeaderConverter}
 */
public class Sbdh2PeppolHeaderParser {

    public static final Logger log = LoggerFactory.getLogger(Sbdh2PeppolHeaderParser.class);

    private final JAXBContext jaxbContext;
    private final XMLInputFactory xmlInputFactory;

    public Sbdh2PeppolHeaderParser() {
        try {
            jaxbContext = JAXBContext.newInstance("org.unece.cefact.namespaces.standardbusinessdocumentheader");
            xmlInputFactory = XMLInputFactory.newInstance();
        } catch (Exception e) {
            String msg = "Unable to initialize the SbdhParser: " + e.getMessage();
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    public PeppolStandardBusinessHeader parse(InputStream inputStream) {

        Unmarshaller unmarshaller = createUnmarshaller();
        XMLStreamReader xmlReader = createXmlStreamReader(inputStream);

        try {
            // Let JAXB unmarshal the SBD/SBDH fragment (skipping payload, since it is commented out in the xsd)
            JAXBElement root = (JAXBElement) unmarshaller.unmarshal(xmlReader);
            StandardBusinessDocument standardBusinessDocument = (StandardBusinessDocument) root.getValue();
            StandardBusinessDocumentHeader standardBusinessDocumentHeader = standardBusinessDocument.getStandardBusinessDocumentHeader();

            PeppolStandardBusinessHeader peppolStandardBusinessHeader = Sbdh2PeppolHeaderConverter.convertSbdh2PeppolHeader(standardBusinessDocumentHeader);

            return peppolStandardBusinessHeader;

        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to parse SBDH: " + e.getMessage(), e);
        }

    }

    private Unmarshaller createUnmarshaller() {
        try {
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to create JAXB unmarshaller: " + e.getMessage(), e);
        }
    }

    private XMLStreamReader createXmlStreamReader(InputStream inputStream) {
        try {
            return xmlInputFactory.createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to crate XML Stream Reader: " + e.getMessage(), e);
        }
    }

}

