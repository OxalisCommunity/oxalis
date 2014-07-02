package eu.peppol.document;

import eu.peppol.PeppolStandardBusinessHeader;
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
import java.util.Date;
import java.util.UUID;

/**
 * @author steinar
 *         Date: 05.11.13
 *         Time: 15:07
 */
public class NoSbdhParser {


    private XPath xPath;
    private Document document;
    private final DocumentBuilderFactory documentBuilderFactory;

    public NoSbdhParser() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    public PeppolStandardBusinessHeader parse(InputStream inputStream) {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(inputStream);

            xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new HardCodedNamespaceResolver());

            PlainUBLHeaderParser plainUBLHeaderParser = new PlainUBLHeaderParser(document, xPath);

            PeppolStandardBusinessHeader sbdh = new PeppolStandardBusinessHeader();
            sbdh.setRecipientId(plainUBLHeaderParser.fetchRecipient());
            sbdh.setSenderId(plainUBLHeaderParser.fetchSender());
            sbdh.setCreationDateAndTime(new Date());
            sbdh.setDocumentTypeIdentifier(plainUBLHeaderParser.fetchDocumentTypeId());
            sbdh.setProfileTypeIdentifier(plainUBLHeaderParser.fetchProcessTypeId());
            sbdh.setMessageId(new MessageId(UUID.randomUUID().toString()));
            return sbdh;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse document " + e.getMessage(), e);
        }

    }

    static class PlainUBLHeaderParser {
        private final Document document;
        private final XPath xPath;

        public PlainUBLHeaderParser(Document document, XPath xPath) {

            this.document = document;
            this.xPath = xPath;
        }

        String localName() {
            return document.getDocumentElement().getLocalName();
        }

        String rootNameSpace() {
            return document.getDocumentElement().getNamespaceURI();
        }

        String ublVersion() {
            return retriveValueForXpath("//cbc:UBLVersionID");
        }

        public CustomizationIdentifier fetchCustomizationId() {
            String value = retriveValueForXpath("//cbc:CustomizationID");
            return CustomizationIdentifier.valueOf(value);
        }

        public PeppolProcessTypeId fetchProcessTypeId() {
            String value = retriveValueForXpath("//cbc:ProfileID");
            return PeppolProcessTypeId.valueOf(value);
        }

        public ParticipantId fetchSender() {
            String endpoint_id_xpath = "//cac:AccountingSupplierParty/cac:Party/cbc:EndpointID";
            String company_id_xpath = "//cac:AccountingSupplierParty/cac:Party/cac:PartyLegalEntity/cbc:CompanyID";
            ParticipantId s;

            try {
                s = participantId(endpoint_id_xpath);
            } catch (IllegalStateException e) {
                s = participantId(company_id_xpath);
            }

            return s;
        }

        public ParticipantId fetchRecipient() {

            String endpoint_id_xpath = "//cac:AccountingCustomerParty/cac:Party/cbc:EndpointID";
            String company_id_xpath = "//cac:AccountingCustomerParty/cac:Party/cac:PartyLegalEntity/cbc:CompanyID";
            ParticipantId s;

            try {
                s = participantId(endpoint_id_xpath);
            } catch (IllegalStateException e) {
                s = participantId(company_id_xpath);
            }

            return s;
        }

        String retriveValueForXpath(String s) {
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


        /**
         * Retrieves the ParticipantId which is held in an XML element, retrieved using the supplied XPath.
         *
         * @param xPathExpr
         * @return
         */
        ParticipantId participantId(String xPathExpr) {
            try {
                Element element = (Element) xPath.evaluate(xPathExpr, document, XPathConstants.NODE);
                if (element == null) {
                    throw new IllegalStateException("No element in XPath: " + xPathExpr);
                }
                String schemeIdTextValue = element.getAttribute("schemeID");
                String companyId = element.getFirstChild().getNodeValue().trim();
                if (schemeIdTextValue == null) {
                    throw new IllegalStateException("Unable to locate schemeID attribute for XPath: " + xPathExpr);
                }
                if (companyId == null) {
                    throw new IllegalStateException("Unable to locate the actual contents for XPath: " + xPathExpr);
                }

                SchemeId schemeId = SchemeId.parse(schemeIdTextValue);

                return new ParticipantId(schemeId.getIso6523Icd() + ":" + companyId);

            } catch (XPathExpressionException e) {
                throw new IllegalStateException(e);
            }
        }


        public PeppolDocumentTypeId fetchDocumentTypeId() {

            CustomizationIdentifier customizationIdentifier = fetchCustomizationId();
            return new PeppolDocumentTypeId(rootNameSpace(), localName(), customizationIdentifier, ublVersion());
        }
    }
}
