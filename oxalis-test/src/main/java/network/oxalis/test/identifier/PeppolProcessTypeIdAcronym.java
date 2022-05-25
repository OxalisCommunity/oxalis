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

package network.oxalis.test.identifier;

import network.oxalis.vefa.peppol.common.model.ProcessIdentifier;

/**
 * Acronyms for the various PEPPOL processes. Makes life a little easier, as the
 * ProcessIdentifier only represents a type safe value of any kind of string.
 * <p>
 * According to Policy 16:
 * <em>PEPPOL processes are identified by the respective BII processes.
 * The process identifier has to match the BII profile ID.</em>
 *
 * @author Steinar Overbeck Cook
 * @see "Tranport Policy for using Identifiers"
 */
public enum PeppolProcessTypeIdAcronym {
    ORDER_ONLY("urn:www.cenbii.eu:profile:bii03:ver1.0"),
    INVOICE_ONLY("urn:www.cenbii.eu:profile:bii04:ver1.0"),
    PROCUREMENT("urn:www.cenbii.eu:profile:bii06:ver1.0");

    private ProcessIdentifier processIdentifier;

    PeppolProcessTypeIdAcronym(String profileId) {
        processIdentifier = ProcessIdentifier.of(profileId);
    }

    public ProcessIdentifier toVefa() {
        return processIdentifier;
    }

    @Override
    public String toString() {
        return processIdentifier.toString();
    }
}
