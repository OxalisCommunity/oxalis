/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.sniffer.document.parsers;

import network.oxalis.sniffer.document.PlainUBLParser;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;

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
    public ParticipantIdentifier getSender() {
        String catalogue = "//cac:ProviderParty/cbc:EndpointID";
        return participantId(catalogue);
    }

    @Override
    public ParticipantIdentifier getReceiver() {
        String catalogue = "//cac:ReceiverParty/cbc:EndpointID";
        return participantId(catalogue);
    }
}
