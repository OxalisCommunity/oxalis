package eu.peppol.document.parsers;

import eu.peppol.document.PlainUBLParser;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.SchemeId;
import org.w3c.dom.Element;

/**
 * Parser to retrieves information from PEPPOL Despatch Advice scenarios.
 * Should be able to decode Despatch Advice document
 *
 * @author thore
 */
public class DespatchAdviceDocumentParser implements PEPPOLDocumentParser {

    private PlainUBLParser parser;

    public DespatchAdviceDocumentParser(PlainUBLParser parser) {
        this.parser = parser;
    }

    @Override
    public ParticipantId getSender() {
        String despatchAdvice = "//cac:DespatchSupplierParty/cac:Party/cbc:EndpointID";
        return participantId(despatchAdvice);
    }

    @Override
    public ParticipantId getReceiver() {
        String despatchAdvice = "//cac:DeliveryCustomerParty/cac:Party/cbc:EndpointID";
        return participantId(despatchAdvice);
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