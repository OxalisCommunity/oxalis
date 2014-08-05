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
