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
import eu.peppol.identifier.ParticipantId;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Takes a document and wraps it together with headers into a StandardBusinessDocument.
 *
 * The SBDH part of the document is constructed from the headers.
 * The document will be the payload (xs:any) following the SBDH.
 *
 * @author thore
 * @author steinar
 */
public class SbdhWrapper {

    /**
     * Wraps payload + headers into a StandardBusinessDocument
     * @param inputStream the input stream to be wrapped
     * @param headers the headers to use for sbdh
     * @return byte buffer with the resulting output in utf-8
     */
    public byte[] wrap(InputStream inputStream, PeppolStandardBusinessHeader headers) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            // create the SBD / SBDH object
            StandardBusinessDocument sbd = new StandardBusinessDocument();
            sbd.setStandardBusinessDocumentHeader(convertPeppolStandardBusinessHeader2StandardBusinessDocumentHeader(headers));
            //sbd.setAny(convertInputStream2XsAnyType(inputStream));  // this was of no use, JAXB changes namespaces of the payload

            // cast to jaxb root element using the utility methods from the ObjectFactory
            ObjectFactory of = new ObjectFactory();
            JAXBElement<StandardBusinessDocument> root = of.createStandardBusinessDocument(sbd);

            // create empty dom document
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // Prevents XML entity expansion attacks
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.newDocument();

            // marshal jaxb element to dom
            JAXBContext jaxbContext = JAXBContext.newInstance("org.unece.cefact.namespaces.standardbusinessdocumentheader");
            Marshaller marshaller = jaxbContext.createMarshaller();
            //marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            //marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(root, document);

            // import the payload element as a new Node and append it last
            Element e = convertInputStream2XsAnyType(inputStream);
            Node n = document.importNode(e, true);
            document.getDocumentElement().appendChild(n);

            // serialize the dom
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(baos);
            transformer.transform(source, result);

        } catch (Exception ex) {
            throw new IllegalStateException("Unable to wrap document inside SBD (SBDH). " + ex.getMessage(), ex);
        }

        return baos.toByteArray();

    }

    private Element convertInputStream2XsAnyType(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        // Prevents XML entity expansion attacks
        dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        return doc.getDocumentElement();
    }

    private StandardBusinessDocumentHeader convertPeppolStandardBusinessHeader2StandardBusinessDocumentHeader(PeppolStandardBusinessHeader headers) throws DatatypeConfigurationException {
        StandardBusinessDocumentHeader sbdh = new StandardBusinessDocumentHeader();
        sbdh.setHeaderVersion("1.0");
        sbdh.getSender().add(getPartner(headers.getSenderId()));
        sbdh.getReceiver().add(getPartner(headers.getRecipientId()));
        sbdh.setDocumentIdentification(getDocumentIdentification(headers));
        // sbdh.setManifest(getManifest(headers));
        sbdh.setBusinessScope(getBusinessScope(headers));
        return sbdh;
    }

    private BusinessScope getBusinessScope(PeppolStandardBusinessHeader headers) {
        BusinessScope b = new BusinessScope();
        b.getScope().add(getScope("DOCUMENTID", headers.getDocumentTypeIdentifier().toString(), null));
        b.getScope().add(getScope("PROCESSID", headers.getProfileTypeIdentifier().toString(), null));
        return b;
    }

    private Scope getScope(String type, String instanceIdentifier, String identifier) {
        Scope s = new Scope();
        s.setType(type);
        s.setInstanceIdentifier(instanceIdentifier);
        s.setIdentifier(identifier);
        return s;
    }

    private DocumentIdentification getDocumentIdentification(PeppolStandardBusinessHeader headers) throws DatatypeConfigurationException {
        DocumentIdentification d = new DocumentIdentification();
        d.setStandard(headers.getDocumentTypeIdentifier().getRootNameSpace());
        d.setTypeVersion(headers.getDocumentTypeIdentifier().getVersion());
        d.setInstanceIdentifier(headers.getMessageId().stringValue());
        d.setType(headers.getDocumentTypeIdentifier().getLocalName());
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        d.setCreationDateAndTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
        return d;
    }

    private Partner getPartner(ParticipantId pid) {
        if (pid == null) return null;
        Partner p = new Partner();
        PartnerIdentification pi = new PartnerIdentification();
        pi.setAuthority("iso6523-actorid-upis");
        pi.setValue(pid.stringValue());
        p.setIdentifier(pi);
        return p;
    }

}
