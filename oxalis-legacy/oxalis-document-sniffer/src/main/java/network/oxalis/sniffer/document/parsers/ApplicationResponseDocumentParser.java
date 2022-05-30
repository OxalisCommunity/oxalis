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
    public ParticipantIdentifier getSender() {
        String applicationResponse = "//cac:SenderParty/cbc:EndpointID";
        return participantId(applicationResponse);
    }

    @Override
    public ParticipantIdentifier getReceiver() {
        String applicationResponse = "//cac:ReceiverParty/cbc:EndpointID";
        return participantId(applicationResponse);
    }
}
