package eu.peppol.document;

import eu.peppol.document.parsers.CatalogueDocumentParser;
import eu.peppol.document.parsers.PEPPOLDocumentParser;
import eu.peppol.document.parsers.InvoiceDocumentParser;
import eu.peppol.document.parsers.OrderDocumentParser;
import eu.peppol.identifier.*;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;

/**
 * Parses the common PEPPOL header information, enough to decide document type and profile
 *
 * @author steinar
 * @author thore
 */
public class PlainUBLHeaderParser extends PlainUBLParser {

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
        System.out.println("Creating DocumentParser for type : " + localName());
        // catalogue scenario
        if ("ApplicationResponse".equalsIgnoreCase(type)) return new CatalogueDocumentParser(this);
        if ("Catalogue".equalsIgnoreCase(type)) return new CatalogueDocumentParser(this);
        // invoice scenario
        if ("CreditNote".equalsIgnoreCase(type)) return new InvoiceDocumentParser(this);
        if ("Invoice".equalsIgnoreCase(type)) return new InvoiceDocumentParser(this);
        if ("Reminder".equalsIgnoreCase(type)) return new InvoiceDocumentParser(this);
        // order scenario
        if ("Order".equalsIgnoreCase(type)) return new OrderDocumentParser(this);
        if ("OrderResponse".equalsIgnoreCase(type)) return new OrderDocumentParser(this);
        // unknown scenario - for now we do  ot have a backup plan
        throw new IllegalStateException("Cannot decide which PEPPOLDocumentParser to use for type " + type);
    }

}
