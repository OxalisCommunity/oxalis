package eu.peppol.outbound.transmission;

import eu.peppol.outbound.lang.OxalisOutboundException;
import eu.peppol.outbound.util.PeekingInputStream;
import eu.peppol.outbound.util.Trace;
import eu.peppol.outbound.util.TraceLogger;
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

    private static final TraceLogger logger = TraceLogger.getLogger(TransmissionRequestFactory.class);

    private final LookupClient lookupClient;

    @Inject
    public TransmissionRequestFactory(LookupClient lookupClient) {
        this.lookupClient = lookupClient;
    }

    public TransmissionRequest newInstance(InputStream inputStream) throws IOException, OxalisOutboundException {
        return newInstance(inputStream, null);
    }

    public TransmissionRequest newInstance(InputStream inputStream, Trace trace) throws IOException, OxalisOutboundException {
        PeekingInputStream peekingInputStream = new PeekingInputStream(inputStream);

        // Read header from content to send.
        logger.debug(trace, "Reading SBDH.");
        Header header;
        try (SbdReader sbdReader = SbdReader.newInstance(peekingInputStream)) {
            header = sbdReader.getHeader();
            logger.info(trace, "Message identifier: {}", header.getIdentifier().getValue());
        } catch (SbdhException e) {
            logger.error(trace, "Unable to read SBDH.", e);
            throw new OxalisOutboundException("Unable to read SBDH.", e);
        }

        // Perform lookup using header.
        logger.debug(trace, "Fetching endpoint information.");
        Endpoint endpoint;
        try {
            endpoint = lookupClient.getEndpoint(header, TransportProfile.AS2_1_0);
        } catch (LookupException | PeppolSecurityException | EndpointNotFoundException e) {
            logger.error(trace, "Failed during lookup of '{}'.", header.getReceiver(), e);
            throw new OxalisOutboundException(String.format("Failed during lookup of '%s'.", header.getReceiver()), e);
        }

        // Create transmission request.
        logger.debug(trace, "Creating transmission request.");
        return new TransmissionRequest(header, peekingInputStream.newInputStream(), endpoint);
    }
}
