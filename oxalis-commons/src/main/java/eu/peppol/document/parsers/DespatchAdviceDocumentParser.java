package eu.peppol.document.parsers;

import eu.peppol.document.PlainUBLParser;
import eu.peppol.identifier.ParticipantId;

/**
 * Parser to retrieves information from PEPPOL Despatch Advice scenarios.
 * Should be able to decode Despatch Advice document
 *
 * @author thore
 */
public class DespatchAdviceDocumentParser extends AbstractDocumentParser {

    public DespatchAdviceDocumentParser(PlainUBLParser parser) {
        super(parser);
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

}