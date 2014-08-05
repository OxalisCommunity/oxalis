package eu.peppol.document;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.parsers.InvoiceDocumentParser;
import eu.peppol.identifier.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;

/**
 * Parses UBL based documents, extracting PeppolStandardBusinessHeader data.
 *
 * @author steinar
 * @author thore
 */
public class NoSbdhParser {

    private final DocumentBuilderFactory documentBuilderFactory;

    public NoSbdhParser() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    public PeppolStandardBusinessHeader parse(InputStream inputStream) {
        try {

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new HardCodedNamespaceResolver());

            PeppolStandardBusinessHeader sbdh = new PeppolStandardBusinessHeader();

            // use the plain UBL header parser to decode format and create correct document parser
            PlainUBLHeaderParser plainUBLHeaderParser = new PlainUBLHeaderParser(document, xPath);
            sbdh.setDocumentTypeIdentifier(plainUBLHeaderParser.fetchDocumentTypeId());
            sbdh.setProfileTypeIdentifier(plainUBLHeaderParser.fetchProcessTypeId());

            // try to use a specialized document parser to fetch more document details
            try {
                PEPPOLDocumentParser peppolDocumentParser = plainUBLHeaderParser.createDocumentParser();
                sbdh.setSenderId(peppolDocumentParser.getSender());
                sbdh.setRecipientId(peppolDocumentParser.getReceiver());
            } catch (Exception ex) {
                /* allow this to happen so that new PEPPOL documents can be used by explicitly setteing sender and receiver thru API */
            }

            return sbdh;

        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse document " + e.getMessage(), e);
        }

    }

}
