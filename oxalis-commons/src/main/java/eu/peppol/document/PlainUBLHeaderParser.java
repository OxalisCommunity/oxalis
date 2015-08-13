package eu.peppol.document;

import eu.peppol.document.parsers.*;
import eu.peppol.identifier.*;
import org.w3c.dom.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.xpath.XPath;

/**
 * Parses the common PEPPOL header information, enough to decide document type and profile
 *
 * @author steinar
 * @author thore
 * @author arun
 */
public class PlainUBLHeaderParser extends PlainUBLParser {

	public static final Logger log = LoggerFactory.getLogger(PlainUBLHeaderParser.class);

    public PlainUBLHeaderParser(Document document, XPath xPath) {
        super(document, xPath);
    }

    public CustomizationIdentifier fetchCustomizationId() {
        String value = retriveValueForXpath("//cbc:CustomizationID");
        return CustomizationIdentifier.valueOf(value);
    }

    public PeppolProcessTypeId fetchProcessTypeId() {
        String value = retriveValueForXpath("//cbc:ProfileID");
        return PeppolProcessTypeId.valueOf(value);
    }

    public PeppolDocumentTypeId fetchDocumentTypeId() {
        CustomizationIdentifier customizationIdentifier = fetchCustomizationId();
        return new PeppolDocumentTypeId(rootNameSpace(), localName(), customizationIdentifier, ublVersion());
    }

    public PEPPOLDocumentParser createDocumentParser() {
        String type = localName();
        log.debug("Creating DocumentParser for type : " + localName());
        // despatch advice scenario
        if ("DespatchAdvice".equalsIgnoreCase(type)) return new DespatchAdviceDocumentParser(this);
        // catalogue scenario
        if ("Catalogue".equalsIgnoreCase(type)) return new CatalogueDocumentParser(this);
        // invoice scenario
        if ("CreditNote".equalsIgnoreCase(type)) return new InvoiceDocumentParser(this);
        if ("Invoice".equalsIgnoreCase(type)) return new InvoiceDocumentParser(this);
        if ("Reminder".equalsIgnoreCase(type)) return new InvoiceDocumentParser(this);
        // order scenario
        if ("Order".equalsIgnoreCase(type)) return new OrderDocumentParser(this);
        if ("OrderResponse".equalsIgnoreCase(type)) return new OrderDocumentParser(this);
        if ("OrderResponseSimple".equalsIgnoreCase(type)) return new OrderDocumentParser(this);
        // application response used by CatalogueResponse, MessageLevelResponse
        if ("ApplicationResponse".equalsIgnoreCase(type)) return new ApplicationResponseDocumentParser(this);
        // unknown scenario - for now we do not have a backup plan
        throw new IllegalStateException("Cannot decide which PEPPOLDocumentParser to use for type " + type);
    }

}
