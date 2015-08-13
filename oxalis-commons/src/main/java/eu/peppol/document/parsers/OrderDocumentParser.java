package eu.peppol.document.parsers;

import eu.peppol.document.PlainUBLParser;
import eu.peppol.identifier.ParticipantId;

/**
 * Parser to retrieves information from PEPPOL Order scenarios.
 * Should be able to decode Order and OrderResponse documents.
 *
 * @author thore
 */
public class OrderDocumentParser extends AbstractDocumentParser {

    public OrderDocumentParser(PlainUBLParser parser) {
        super(parser);
    }

    @Override
    public ParticipantId getSender() {
        String xpath = "//cac:BuyerCustomerParty/cac:Party/cbc:EndpointID";
        if (parser.localName().startsWith("OrderResponse")) {
            // Matches both OrderResponse and OrderResponseSimple
            xpath = "//cac:SellerSupplierParty/cac:Party/cbc:EndpointID";
        }
        return participantId(xpath);
    }

    @Override
    public ParticipantId getReceiver() {
        String xpath = "//cac:SellerSupplierParty/cac:Party/cbc:EndpointID";
        if (parser.localName().startsWith("OrderResponse")) {
            // Matches both OrderResponse and OrderResponseSimple
            xpath = "//cac:BuyerCustomerParty/cac:Party/cbc:EndpointID";
        }
        return participantId(xpath);
    }

}