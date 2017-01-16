package eu.peppol.outbound.lookup;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.vefa.peppol.common.lang.EndpointNotFoundException;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.api.LookupException;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;

import java.util.List;

/**
 * Default implementation of {@link LookupService}. This implementation performs no caching except caching part of
 * underlaying implementations.
 */
class DefaultLookupService implements LookupService {

    /**
     * LookupClient provided by VEFA PEPPOL project.
     */
    private final LookupClient lookupClient;

    /**
     * Prioritized list of supported transport profiles detected in
     * {@link eu.peppol.outbound.transmission.MessageSenderFactory}.
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
