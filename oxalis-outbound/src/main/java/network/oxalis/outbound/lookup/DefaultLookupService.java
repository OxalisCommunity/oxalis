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

package network.oxalis.outbound.lookup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.lookup.LookupService;
import network.oxalis.api.util.Type;
import network.oxalis.vefa.peppol.common.lang.EndpointNotFoundException;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.TransportProfile;
import network.oxalis.vefa.peppol.lookup.LookupClient;
import network.oxalis.vefa.peppol.lookup.api.LookupException;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;

import java.util.List;

/**
 * Default implementation of {@link LookupService}. This implementation performs no caching except caching part of
 * underlying implementations.
 *
 * @author erlend
 * @since 4.0.0
 */
@Singleton
@Type("default")
class DefaultLookupService implements LookupService {

    /**
     * LookupClient provided by VEFA PEPPOL project.
     */
    private final LookupClient lookupClient;

    /**
     * Prioritized list of supported transport profiles detected.
     */
    private final TransportProfile[] transportProfiles;

    @Inject
    public DefaultLookupService(LookupClient lookupClient,
                                @Named("prioritized") List<TransportProfile> transportProfiles) {
        this.lookupClient = lookupClient;
        this.transportProfiles = transportProfiles.toArray(new TransportProfile[transportProfiles.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Endpoint lookup(Header header) throws OxalisTransmissionException {
        try {
            return lookupClient.getEndpoint(header, transportProfiles);
        } catch (LookupException | PeppolSecurityException | EndpointNotFoundException e) {
            throw new OxalisTransmissionException(e.getMessage(), e);
        }
    }
}
