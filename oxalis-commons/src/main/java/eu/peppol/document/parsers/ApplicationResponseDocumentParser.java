package eu.peppol.document.parsers;

import eu.peppol.document.PlainUBLParser;
import eu.peppol.identifier.ParticipantId;

/**
 * Parser to retrieves information from PEPPOL Application Response documents.
 * Should be able to decode Catalogue Response, Message Level Response and others based on ApplicationResponse
 *
 * @author thore
 */
public class ApplicationResponseDocumentParser extends AbstractDocumentParser {

    public ApplicationResponseDocumentParser(PlainUBLParser parser) {
        super(parser);
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

}