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

package eu.peppol.service;

import eu.peppol.identifier.Endpoint;
import eu.peppol.identifier.MessageHeader;
import eu.peppol.lang.OxalisLookupException;
import eu.peppol.lang.OxalisSecurityException;

public interface LookupService {

    /**
     * Fetch endpoint information for a receiving endpoint.
     *
     * @param header Header of message to send.
     * @param transportProfiles Supported transport profiles by current Oxalis instance.
     * @return Endpoint information for use when sending.
     * @throws OxalisLookupException Thrown in case of unable to lookup, lack of supporting transport profiles and more.
     * @throws OxalisSecurityException Thrown in case of security reasons like invalid certificate and signature errors.
     */
    Endpoint getEndpoint(MessageHeader header, /* TransportProfile */ String... transportProfiles) throws OxalisLookupException, OxalisSecurityException;

}
