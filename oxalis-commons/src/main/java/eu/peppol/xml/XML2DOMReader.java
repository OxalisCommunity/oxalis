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

package eu.peppol.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Attempts to parse an XML file using SAX, searching for a given element, which is converted into a DOM object, which
 * is suitable for XPath application.
 *
 * <em>Beware: instances of this class is not thread safe</em>
 *
 * @author steinar
 *         Date: 24.06.15
 *         Time: 16.07
 *
 * @author Forent Georges
 * @see <a href="http://fgeorges.blogspot.no/2006/08/translate-sax-events-to-dom-tree.html">Translate SAX events to a DOM tree</a>
 */
public class XML2DOMReader extends DefaultHandler {

    public static final Logger log = LoggerFactory.getLogger(XML2DOMReader.class);

    /** Number of start elements to read before aborting unless the supplied xml element has been seen. */
    public static final int START_ELEMENTS_THRESHOLD = 10;

    private final Document document;
    private Node myCurrentNode;

    // Name of XML element to extract into W3C DOM Document
    private String elementToRead;

    // Indicates whether "elementToRead" has been seen yet.
    private boolean parsingActivated = false;

    // Number of start elements seen
    int startElementCounter = 0;

    public XML2DOMReader() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            // Prevents XML entity expansion attacks. See https://github.com/difi/oxalis/issues/208
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,  true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
            myCurrentNode = document;
            parsingActivated = false;

        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Inspects the supplied XML element name. If we see the start tag of the element we are looking for,
     * we parse every XML element into a DOM node.
     *
     * @param uri
     * @param name
     * @param qName
     * @param attrs
     * @throws StopSaxParserException
     */
    @Override
    public void startElement(String uri, String name, String qName, Attributes attrs) throws StopSaxParserException {

        startElementCounter++;

        // If this is the element we want to parse, activate parsing
        if (!parsingActivated && qName.equals(elementToRead)) {
            parsingActivated = true;
        }

        // If parsing has not started after START_ELEMENTS_THRESHOLD, we abort parsing.
        if (!parsingActivated && startElementCounter > START_ELEMENTS_THRESHOLD) {
            log.warn("Parsing aborted after " + START_ELEMENTS_THRESHOLD + " XML start elements");
            stopSaxParsing();
        }

        // We do nothing until we have seen the start tag as supplied
        if (!parsingActivated) {
            return;
        }

        // Creates the DOM element.
        Element elem = document.createElementNS(uri, qName);

        // Adds each attribute.
        for (int i = 0; i < attrs.getLength(); ++i) {
            String ns_uri = attrs.getURI(i);
            String qname = attrs.getQName(i);
            String value = attrs.getValue(i);
            Attr attr = document.createAttributeNS(ns_uri, qname);
            attr.setValue(value);
            elem.setAttributeNodeNS(attr);
        }

        // Appends the node into the DOM tree
        myCurrentNode.appendChild(elem);
        myCurrentNode = elem;
    }


    // Adjust the current place for subsequent additions.
    @Override
    public void endElement(String uri, String name, String qName) throws StopSaxParserException {
        if (!parsingActivated){
            return;
        }
        myCurrentNode = myCurrentNode.getParentNode();
        if (elementToRead.equals(qName)) {
            stopSaxParsing();
        }
    }

    protected void stopSaxParsing() throws StopSaxParserException {
        throw new StopSaxParserException();
    }


    // Add a new text node in the DOM tree, at the right place.
    @Override
    public void characters(char[] ch, int start, int length) {
        if (!parsingActivated){
            return;
        }
        String str  = new String(ch, start, length);
        Text text = document.createTextNode(str);
        myCurrentNode.appendChild(text);
    }

    // Add a new text node in the DOM tree, at the right place.
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) {
        if (!parsingActivated){
            return;
        }
        String str  = new String(ch, start, length);
        Text   text = document.createTextNode(str);
        myCurrentNode.appendChild(text);
    }

    // Add a new text PI in the DOM tree, at the right place.
    @Override
    public void processingInstruction(String target, String data) {
        if (!parsingActivated){
            return;
        }
        ProcessingInstruction pi = document.createProcessingInstruction(target, data);
        myCurrentNode.appendChild(pi);
    }

    // For the handlers below, use your usual logging facilities.
    public void error(SAXParseException e) {
        throw new IllegalStateException("Error at line " + e.getLineNumber() + ", column " + e.getColumnNumber() + " : " + e.getMessage(), e);
    }

    public void fatalError(SAXParseException e) {
        throw new IllegalStateException("Fatal error at line " + e.getLineNumber() + ", column " + e.getColumnNumber() + " : " + e.getMessage(), e);
    }

    public void warning(SAXParseException e) {
        System.err.println("Warning : " + e.getMessage());
    }


    /**
     * Parses the inputstream until <em>elementToRead</em> has been seen, after which the entire element
     * is converted into a W3C DOM Document object.
     *
     * If the <em>elemtnToRead</em> has not been seen after reading {@link #START_ELEMENTS_THRESHOLD} start elements,
     * parsing is aborted.
     *
     * @param inputStream
     * @param elementToRead
     * @return
     */
    public Document parse(InputStream inputStream, String elementToRead) {
        this.elementToRead = elementToRead;

        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            // Prevents XML entity expansion attacks, see https://github.com/difi/oxalis/issues/208
            saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);

            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();

            xmlReader.setContentHandler(this);

            xmlReader.parse(new InputSource(inputStream));

            return document;
        } catch(StopSaxParserException e){
            return document;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Custom Exception used to abort the SAX call back parser.
     */
    private class StopSaxParserException extends SAXException {
    }
}
