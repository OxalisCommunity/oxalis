package eu.peppol.outbound.transmission;

import brave.Span;
import brave.Tracer;
import com.google.common.io.ByteStreams;
import com.google.inject.name.Named;
import eu.peppol.document.NoSbdhParser;
import eu.peppol.identifier.MessageId;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.commons.io.PeekingInputStream;
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
import java.util.List;

public class TransmissionRequestFactory {

    private final LookupClient lookupClient;

    private final NoSbdhParser noSbdhParser;

    private final TransportProfile[] transportProfiles;

    private final Tracer tracer;

    @Inject
    public TransmissionRequestFactory(LookupClient lookupClient, NoSbdhParser noSbdhParser,
                                      @Named("prioritized") List<TransportProfile> transportProfiles, Tracer tracer) {
        this.lookupClient = lookupClient;
        this.noSbdhParser = noSbdhParser;
        this.transportProfiles = transportProfiles.toArray(new TransportProfile[transportProfiles.size()]);
        this.tracer = tracer;
    }

    public TransmissionRequest newInstance(InputStream inputStream) throws IOException, OxalisTransmissionException {
        try (Span root = tracer.newTrace().name(getClass().getSimpleName()).start()) {
            return createInstance(inputStream, root);
        }
    }

    public TransmissionRequest newInstance(InputStream inputStream, Span root) throws IOException, OxalisTransmissionException {
        try (Span span = tracer.newChild(root.context()).name(getClass().getSimpleName()).start()) {
            return createInstance(inputStream, span);
        }
    }

    private TransmissionRequest createInstance(InputStream inputStream, Span root) throws IOException, OxalisTransmissionException {
        PeekingInputStream peekingInputStream = new PeekingInputStream(inputStream);

        // Read header from content to send.
        Header header;
        try {
            // Read header from SBDH.
            try (Span span = tracer.newChild(root.context()).name("Reading SBDH").start()) {
                try (SbdReader sbdReader = SbdReader.newInstance(peekingInputStream)) {
                    header = sbdReader.getHeader();
                    span.tag("identifier", header.getIdentifier().getValue());
                } catch (SbdhException e) {
                    span.tag("exception", e.getMessage());
                    throw e;
                }
            }
        } catch (SbdhException e) {
            // Reading complete document to memory. Sorry!
            byte[] payload;
            try (Span span = tracer.newChild(root.context()).name("Read content to memory").start()) {
                payload = ByteStreams.toByteArray(peekingInputStream.newInputStream());
                span.tag("size", String.valueOf(payload.length));
            }

            // Detect header from content.
            try (Span span = tracer.newChild(root.context()).name("Detect SBDH from content").start()) {
                try {
                    header = noSbdhParser.parse(new ByteArrayInputStream(payload)).toVefa();
                    span.tag("identifier", header.getIdentifier().getValue());
                } catch (IllegalStateException ex) {
                    span.tag("exception", ex.getMessage());
                    throw new OxalisTransmissionException(ex.getMessage(), ex);
                }
            }

            // Wrap content in SBDH.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (Span span = tracer.newChild(root.context()).name("Wrap content in SBDH").start()) {
                try (SbdWriter sbdWriter = SbdWriter.newInstance(outputStream, header)) {
                    XMLStreamUtils.copy(new ByteArrayInputStream(payload), sbdWriter.xmlWriter());
                } catch (SbdhException | XMLStreamException ex) {
                    span.tag("exception", ex.getMessage());
                    throw new OxalisTransmissionException("Unable to wrap content in SBDH.", ex);
                }
            }

            // Preparing wrapped content for sending.
            peekingInputStream = new PeekingInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
        }

        // Perform lookup using header.
        Endpoint endpoint;
        try (Span span = tracer.newChild(root.context()).name("Fetch endpoint information").start()) {
            try {
                endpoint = lookupClient.getEndpoint(header, transportProfiles);
                span.tag("transport profile", endpoint.getTransportProfile().getValue());
            } catch (LookupException | PeppolSecurityException | EndpointNotFoundException e) {
                span.tag("exception", e.getMessage());
                throw new OxalisTransmissionException(String.format("Failed during lookup of '%s'.", header.getReceiver()), e);
            }
        }

        // Create transmission request.
        return new DefaultTransmissionRequest(new MessageId(), header, peekingInputStream.newInputStream(), endpoint);
    }
}
