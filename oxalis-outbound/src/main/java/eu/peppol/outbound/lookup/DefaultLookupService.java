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

class DefaultLookupService implements LookupService {

    private LookupClient lookupClient;

    private TransportProfile[] transportProfiles;

    @Inject
    public DefaultLookupService(LookupClient lookupClient, @Named("prioritized") List<TransportProfile> transportProfiles) {
        this.lookupClient = lookupClient;
        this.transportProfiles = transportProfiles.toArray(new TransportProfile[transportProfiles.size()]);
    }

    public Endpoint lookup(Header header) throws OxalisTransmissionException {
        try {
            return lookupClient.getEndpoint(header, transportProfiles);
        } catch (LookupException | PeppolSecurityException | EndpointNotFoundException e) {
            throw new OxalisTransmissionException(e.getMessage(), e);
        }
    }
}
