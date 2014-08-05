package eu.peppol.document.parsers;

import eu.peppol.document.PEPPOLDocumentParser;
import eu.peppol.document.PlainUBLParser;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.SchemeId;
import org.w3c.dom.Element;

/**
 * Parser to retrieves information from PEPPOL Catalogue scenarios.
 * Should be able to decode Catalogue and catalogue response (ApplicationResponse)
 *
 * @author thore
 */
public class CatalogueDocumentParser implements PEPPOLDocumentParser {

    private PlainUBLParser parser;

    public CatalogueDocumentParser(PlainUBLParser parser) {
        this.parser = parser;
    }

    @Override
    public ParticipantId getSender() {
        String catalogue = "//cac:ProviderParty/cbc:EndpointID";
        String applicationResponse = "//cac:SenderParty/cbc:EndpointID";
        ParticipantId s;
        try {
            s = participantId(catalogue);
        } catch (IllegalStateException e) {
            s = participantId(applicationResponse);
        }
        return s;
    }

    @Override
    public ParticipantId getReceiver() {
        String catalogue = "//cac:ReceiverParty/cbc:EndpointID";
        String applicationResponse = "//cac:ReceiverParty/cbc:EndpointID";
        ParticipantId s;
        try {
            s = participantId(catalogue);
        } catch (IllegalStateException e) {
            s = participantId(applicationResponse);
        }
        return s;
    }

    /**
     * Retrieves the ParticipantId which is held in an XML element, retrieved using the supplied XPath.
     *
     * @param xPathExpr
     * @return
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