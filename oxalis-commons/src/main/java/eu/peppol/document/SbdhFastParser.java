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

import eu.peppol.xml.XML2DOMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.ManifestItem;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * An implementation of SBDH parser, which is optimized for speed on large files.
 *
 * It will first use a SAX parser to extract the <code>StandardBusinessDocumentHeader</code> only and
 * create a W3C DOM object.
 *
 * The W3C Document is then fed into JAXB, which saves us all the hassle of using Xpath to extract the data.
 *
 * This class is not thread safe.
 *
 * @author steinar
 *         Date: 24.06.15
 *         Time: 15.58
 */
public class SbdhFastParser {

    private static final Logger log = LoggerFactory.getLogger(SbdhFastParser.class);
    private JAXBContext jaxbContext;

    public SbdhFastParser() {
        // Do this only once, as it is pretty heavy
        try {
            // supplying the Class loader should hopefully make this work in a JEE environment
            jaxbContext = JAXBContext.newInstance("org.unece.cefact.namespaces.standardbusinessdocumentheader", org.unece.cefact.namespaces.standardbusinessdocumentheader.ObjectFactory.class.getClassLoader());
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Parses the inputstream from first occurence of &lt;StandardBusinessDocumentHeader&gt; to
     * the corresponding &lt;/StandardBusinessDocumentHeader&gt; into a W3C DOM object, after which the DOM
     * is unmarshalled into an Object graph using JaxB.
     *
     * Not very pretty, but it improves speed a lot when you have large XML documents.
     *
     * @param inputStream the inputstream containing the XML
     * @return an instance of StandardBusinessDocumentHeader if found, otherwise null.
     */
    public StandardBusinessDocumentHeader parse(InputStream inputStream) {

        StandardBusinessDocumentHeader standardBusinessDocumentHeader = null;

        if (inputStream.markSupported()) {
            // Indicates number of bytes to be read before the mark position is invalidated
            inputStream.mark(1024*32); // 32K should be sufficient to read the SBDH
        }

        // Parses and creates the W3C DOM
        Document document = parseSbdhIntoW3CDocument(inputStream);

        // If input stream contained an SBDH, unmarshal it to an Object graph using JaxB
        if (sbdhFoundInDocument(document)) {

            Unmarshaller unmarshaller = createUnmarshaller();

            // Let JAXB unmarshal into a Java Object graph from the W3C DOM Document object.
            JAXBElement root = null;
            try {
                root = (JAXBElement) unmarshaller.unmarshal(document);
            } catch (JAXBException e) {
                throw new IllegalStateException("Unable to unmarshal :" + e, e);
            }

            // Tada!
            standardBusinessDocumentHeader = (StandardBusinessDocumentHeader) root.getValue();
        }

        if (inputStream.markSupported()) {
            try {
                inputStream.reset();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to reset intput stream" + e,e);
            }
        }
        return standardBusinessDocumentHeader;
    }


    /**
     * If the supplied W3C Document contains data, we assume an SBDH was detected.
     *
     * @param document W3C Document holding the SBDH
     * @return true if Document object contains child nodes.
     */
    protected boolean sbdhFoundInDocument(Document document) {
        return document.getChildNodes().getLength() > 0;
    }

    /**
     * Parses the XML inputstream using
     * @param inputStream
     * @return
     */
    protected Document parseSbdhIntoW3CDocument(InputStream inputStream) {
        XML2DOMReader xml2DOMReader = new XML2DOMReader();
        return xml2DOMReader.parse(inputStream, "StandardBusinessDocumentHeader");
    }

    private Unmarshaller createUnmarshaller() {
        try {
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to create JAXB unmarshaller: " + e.getMessage(), e);
        }
    }

    /**
     * Transforms the W3C DOM object into a pretty printed string.
     *
     * @param document
     * @return
     */
    String prettyPrint(Document document) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            String s = result.getWriter().toString();
            return s;
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ManifestItem searchForAsicManifestItem(StandardBusinessDocumentHeader sbdh) {

        if (sbdh.getManifest() != null && sbdh.getManifest().getNumberOfItems().intValue() > 0) {
            for (ManifestItem manifestItem : sbdh.getManifest().getManifestItem()) {
                if (manifestItem.getMimeTypeQualifierCode().equals("application/vnd.etsi.asic-e+zip") ){
                    return manifestItem;
                }
            }
        }
        // Not found
        return null;
    }

}
