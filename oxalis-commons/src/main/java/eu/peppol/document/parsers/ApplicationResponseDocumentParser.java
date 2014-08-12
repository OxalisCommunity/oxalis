package eu.peppol.document.parsers;

import eu.peppol.document.PlainUBLParser;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.SchemeId;
import org.w3c.dom.Element;

/**
 * Parser to retrieves information from PEPPOL Application Response documents.
 * Should be able to decode Catalogue Response, Message Level Response and others based on ApplicationResponse
 *
 * @author thore
 */
public class ApplicationResponseDocumentParser implements PEPPOLDocumentParser {

    private PlainUBLParser parser;

    public ApplicationResponseDocumentParser(PlainUBLParser parser) {
        this.parser = parser;
    }

    @Override
    public ParticipantId getSender() {
        String applicationResponse = "//cac:SenderParty/cbc:EndpointID";
        return participantId(applicationResponse);
    }

    @Override
    public ParticipantId getReceiver() {
        String applicationResponse = "//cac:ReceiverParty/cbc:EndpointID";
        return participantId(applicationResponse);
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