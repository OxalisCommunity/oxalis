/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package no.difi.oxalis.sniffer.document.parsers;

import eu.peppol.identifier.ParticipantId;

/**
 * A small set of common information we should be able to retrieve from any PEPPOL UBL/EHF document.
 *
 * @author thore
 */
public interface PEPPOLDocumentParser {

    /**
     * Identify and return the PEPPOL participant sending the document.
     */
    ParticipantId getSender();

    /**
     * Identify and return the PEPPOL participant receiving the document.
     */
    ParticipantId getReceiver();

}
