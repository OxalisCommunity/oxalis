/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

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