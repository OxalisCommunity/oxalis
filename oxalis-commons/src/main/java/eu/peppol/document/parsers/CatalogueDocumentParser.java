package eu.peppol.document.parsers;

import eu.peppol.document.PlainUBLParser;
import eu.peppol.identifier.ParticipantId;

/**
 * Parser to retrieves information from PEPPOL Catalogue scenarios.
 * Should be able to decode Catalogue (for catalogue response see ApplicationResponse)
 *
 * @author thore
 */
public class CatalogueDocumentParser extends AbstractDocumentParser {

    public CatalogueDocumentParser(PlainUBLParser parser) {
        super(parser);
    }

    @Override
    public ParticipantId getSender() {
        String catalogue = "//cac:ProviderParty/cbc:EndpointID";
        return participantId(catalogue);
    }

    @Override
    public ParticipantId getReceiver() {
        String catalogue = "//cac:ReceiverParty/cbc:EndpointID";
        return participantId(catalogue);
    }

}