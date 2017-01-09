package eu.peppol.outbound.transmission;

import eu.peppol.outbound.lang.OxalisOutboundException;
import eu.peppol.outbound.util.PeekingInputStream;
import no.difi.vefa.peppol.common.lang.EndpointNotFoundException;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.api.LookupException;
import no.difi.vefa.peppol.sbdh.SbdReader;
import no.difi.vefa.peppol.sbdh.lang.SbdhException;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

public class TransmissionRequestFactory {

    private final LookupClient lookupClient;

    @Inject
    public TransmissionRequestFactory(LookupClient lookupClient) {
        this.lookupClient = lookupClient;
    }

    public TransmissionRequest newInstance(InputStream inputStream) throws IOException, OxalisOutboundException {
        PeekingInputStream peekingInputStream = new PeekingInputStream(inputStream);

        // Read header from content to send.
        Header header;
        try (SbdReader sbdReader = SbdReader.newInstance(peekingInputStream)) {
            header = sbdReader.getHeader();
        } catch (SbdhException e) {
            throw new OxalisOutboundException("Unable to read SBDH.", e);
        }

        // Perform lookup using header
        Endpoint endpoint;
        try {
            endpoint = lookupClient.getEndpoint(header, TransportProfile.AS2_1_0);
        } catch (LookupException | PeppolSecurityException | EndpointNotFoundException e) {
            throw new OxalisOutboundException(String.format("Failed during lookup of '%s'.", header.getReceiver()), e);
        }

        return new TransmissionRequest(header, peekingInputStream.newInputStream(), endpoint);
    }
}
