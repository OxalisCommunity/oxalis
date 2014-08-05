package eu.peppol.document.parsers;

import eu.peppol.document.PEPPOLDocumentParser;
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

    @Override
    public ParticipantId getReceiver() {
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

    /**
     * Retrieves the ParticipantId which is held in an XML element, retrieved using the supplied XPath.
     */
    private ParticipantId participantId(String xPathExpr) {
        Element element = parser.retrieveElementForXpath(xPathExpr);
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
    }

}
