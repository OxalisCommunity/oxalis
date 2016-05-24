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

package no.difi.oxalis.smp.service;

import eu.peppol.identifier.MessageHeader;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.lang.OxalisLookupException;
import eu.peppol.lang.OxalisSecurityException;
import eu.peppol.service.LookupService;
import no.difi.oxalis.smp.identifier.EndpointWrapper;
import no.difi.vefa.peppol.common.code.Iso6523Icd;
import no.difi.vefa.peppol.common.lang.EndpointNotFoundException;
import no.difi.vefa.peppol.common.model.*;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.LookupClientBuilder;
import no.difi.vefa.peppol.lookup.api.LookupException;
import no.difi.vefa.peppol.security.api.PeppolSecurityException;

/**
 * Implements SMP lookup.
 */
public class SmpLookupService implements LookupService {

    private LookupClient lookupClient;

    public SmpLookupService() {
        // Initiate client
        lookupClient = LookupClientBuilder.forProduction().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EndpointWrapper getEndpoint(MessageHeader header, String... transportProfiles) throws OxalisLookupException, OxalisSecurityException {
        try {
            // Generate objects representing header content
            ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(Iso6523Icd.valueOfIcd(header.getTo().toString()), ParticipantId.getScheme());
            DocumentTypeIdentifier documentIdentifier = new DocumentTypeIdentifier(header.getDocumentIdentifier().toString());
            ProcessIdentifier processIdentifier = new ProcessIdentifier(header.getProcessIdentifier().toString());

            // Generate objects for transport profiles
            TransportProfile[] transportProfileList = new TransportProfile[transportProfiles.length];
            for (int i = 0; i < transportProfiles.length; i++)
                transportProfileList[i] = new TransportProfile(transportProfiles[i]);

            // Fetch endpoint
            Endpoint endpoint = lookupClient.getEndpoint(participantIdentifier, documentIdentifier, processIdentifier, transportProfileList);

            // Throw exception in case of no endpoint is found
            if (endpoint == null)
                throw new OxalisLookupException("Unable to find endpoint information for process identifier and transport profile(s).");

            // Return endpoint inside wrapper for Oxalis
            return new EndpointWrapper(endpoint);
        } catch (PeppolSecurityException e) {
            // Handle security exception to become the Oxalis security exception
            throw new OxalisSecurityException(e.getMessage(), e);
        } catch (EndpointNotFoundException | LookupException e) {
            // Other exceptions, like for lookup exception, becomes Oxalis lookup exception
            throw new OxalisLookupException(e.getMessage(), e);
        }
    }
}
