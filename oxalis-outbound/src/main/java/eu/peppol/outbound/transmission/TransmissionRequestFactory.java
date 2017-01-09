package eu.peppol.outbound.transmission;

import com.google.common.io.ByteStreams;
import eu.peppol.document.NoSbdhParser;
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
import no.difi.vefa.peppol.sbdh.SbdWriter;
import no.difi.vefa.peppol.sbdh.lang.SbdhException;
import no.difi.vefa.peppol.sbdh.util.XMLStreamUtils;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TransmissionRequestFactory {

    private static final TraceLogger logger = TraceLogger.getLogger(TransmissionRequestFactory.class);

    private final LookupClient lookupClient;

    private final NoSbdhParser noSbdhParser;

    @Inject
    public TransmissionRequestFactory(LookupClient lookupClient, NoSbdhParser noSbdhParser) {
        this.lookupClient = lookupClient;
        this.noSbdhParser = noSbdhParser;
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
            // Read header from SBDH.
            header = sbdReader.getHeader();
            logger.info(trace, "Message identifier: {}", header.getIdentifier().getValue());
        } catch (SbdhException e) {
            // Detect header from content.
            logger.info(trace, "SBDH not found, trying to detect SBDH data from content.", e);

            // Reading complete document to memory. Sorry!
            byte[] payload = ByteStreams.toByteArray(peekingInputStream.newInputStream());

            try {
                header = noSbdhParser.parse(new ByteArrayInputStream(payload)).toVefa();
            } catch (IllegalStateException ex) {
                logger.error(trace, "Unable to detect SBDH data from content.", ex);
                throw new OxalisOutboundException(ex.getMessage(), ex);
            }

            logger.info(trace, "Message identifier: {}", header.getIdentifier().getValue());

            // Wrap content in SBDH.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (SbdWriter sbdWriter = SbdWriter.newInstance(outputStream, header)) {
                XMLStreamUtils.copy(new ByteArrayInputStream(payload), sbdWriter.xmlWriter());
            } catch (SbdhException | XMLStreamException ex) {
                logger.error(trace, "Unable to wrap content in SBDH", ex);
                throw new OxalisOutboundException("Unable to wrap content in SBDH.", ex);
            }

            logger.info(trace, "Wrapping content in SBDH finished.");

            // Preparing wrapped content for sending.
            peekingInputStream = new PeekingInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
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
