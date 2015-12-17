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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

/**
 * Simple parser that are UBL aware and handles xpath with namespaces.
 *
 * @author thore
 */
public class PlainUBLParser {

    private final Document document;
    private final XPath xPath;

    public PlainUBLParser(Document document, XPath xPath) {
        this.document = document;
        this.xPath = xPath;
    }

    public String localName() {
        return document.getDocumentElement().getLocalName();
    }

    public String rootNameSpace() {
        return document.getDocumentElement().getNamespaceURI();
    }

    public String ublVersion() {
        return retriveValueForXpath("//cbc:UBLVersionID");
    }

    public boolean canParse() {
        return ("" + rootNameSpace()).startsWith("urn:oasis:names:specification:ubl:schema:xsd:");
    }

    public Element retrieveElementForXpath(String s) {
        try {
            Element element = (Element) xPath.evaluate(s, document, XPathConstants.NODE);
            if (element == null) {
                throw new IllegalStateException("No element in XPath: " + s);
            }
            return element;
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Unable to evaluate " + s + "; " + e.getMessage(), e);
        }
    }

    public String retriveValueForXpath(String s) {
        try {
            String value = xPath.evaluate(s, document);
            if (value == null) {
                throw new IllegalStateException("Unable to find value for Xpath expr " + s);
            }
            return value.trim();
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Unable to evaluate " + s + "; " + e.getMessage(), e);
        }
    }

}
