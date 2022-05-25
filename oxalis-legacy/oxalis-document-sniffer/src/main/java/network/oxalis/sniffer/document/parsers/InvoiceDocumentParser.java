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
 * Parser to retrieves information from PEPPOL Invoice scenarios.
 * Should be able to decode Invoices in plain UBL and Norwegian EHF variants.
 *
 * @author thore
 */
public class InvoiceDocumentParser extends AbstractDocumentParser {

    public InvoiceDocumentParser(PlainUBLParser parser) {
        super(parser);
    }

    @Override
    public ParticipantIdentifier getSender() {
        String endpointFirst = "//cac:AccountingSupplierParty/cac:Party/cbc:EndpointID";
        String companySecond = "//cac:AccountingSupplierParty/cac:Party/cac:PartyLegalEntity/cbc:CompanyID";
        ParticipantIdentifier s;
        try {
            s = participantId(endpointFirst);
        } catch (IllegalStateException e) {
            s = participantId(companySecond);
        }
        return s;
    }

    @Override
    public ParticipantIdentifier getReceiver() {
        String endpointFirst = "//cac:AccountingCustomerParty/cac:Party/cbc:EndpointID";
        String companySecond = "//cac:AccountingCustomerParty/cac:Party/cac:PartyLegalEntity/cbc:CompanyID";
        ParticipantIdentifier s;
        try {
            s = participantId(endpointFirst);
        } catch (IllegalStateException e) {
            s = participantId(companySecond);
        }
        return s;
    }

}
