package eu.peppol.document.parsers;

import eu.peppol.document.PlainUBLParser;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.SchemeId;
import org.w3c.dom.Element;

/**
 * Parser to retrieves information from PEPPOL Invoice scenarios.
 * Should be able to decode Invoices in plain UBL and Norwegian EHF variants.
 *
 * @author thore
 */
public class InvoiceDocumentParser implements PEPPOLDocumentParser {

    private PlainUBLParser parser;

    public InvoiceDocumentParser(PlainUBLParser parser) {
        this.parser = parser;
    }

    @Override
    public ParticipantId getSender() {
        String endpointFirst = "//cac:AccountingSupplierParty/cac:Party/cbc:EndpointID";
        String companySecond = "//cac:AccountingSupplierParty/cac:Party/cac:PartyLegalEntity/cbc:CompanyID";
        ParticipantId s;
        try {
            s = participantId(endpointFirst);
        } catch (IllegalStateException e) {
            s = participantId(companySecond);
        }
        return s;
    }

    @Override
    public ParticipantId getReceiver() {
        String endpointFirst = "//cac:AccountingCustomerParty/cac:Party/cbc:EndpointID";
        String companySecond = "//cac:AccountingCustomerParty/cac:Party/cac:PartyLegalEntity/cbc:CompanyID";
        ParticipantId s;
        try {
            s = participantId(endpointFirst);
        } catch (IllegalStateException e) {
            s = participantId(companySecond);
        }
        return s;
    }

    /**
     * Retrieves the ParticipantId which is held in an XML element, retrieved using the supplied XPath.
     * Note : DOM parser throws "java.lang.IllegalStateException: No element in XPath: ..." of no Element is found
     */
    private ParticipantId participantId(String xPathExpr) {
        Element element = parser.retrieveElementForXpath(xPathExpr);
        String schemeIdTextValue = element.getAttribute("schemeID").trim();
        String companyId = element.getFirstChild().getNodeValue().trim();
        if (schemeIdTextValue.length() > 0) companyId = SchemeId.parse(schemeIdTextValue).getIso6523Icd() + ":" + companyId;
        return new ParticipantId(companyId);
    }

}
