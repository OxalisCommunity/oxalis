package eu.peppol.outbound.transmission;

import brave.Span;
import brave.Tracer;
import com.google.common.io.ByteStreams;
import eu.peppol.document.NoSbdhParser;
import eu.peppol.identifier.MessageId;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.commons.io.PeekingInputStream;
import no.difi.oxalis.commons.tracing.Traceable;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.sbdh.SbdReader;
import no.difi.vefa.peppol.sbdh.SbdWriter;
import no.difi.vefa.peppol.sbdh.lang.SbdhException;
import no.difi.vefa.peppol.sbdh.util.XMLStreamUtils;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TransmissionRequestFactory extends Traceable {

    private final LookupService lookupService;

    private final NoSbdhParser noSbdhParser;

    @Inject
    public TransmissionRequestFactory(LookupService lookupService, NoSbdhParser noSbdhParser, Tracer tracer) {
        super(tracer);
        this.lookupService = lookupService;
        this.noSbdhParser = noSbdhParser;
    }

    public TransmissionRequest newInstance(InputStream inputStream) throws IOException, OxalisTransmissionException {
        try (Span root = tracer.newTrace().name(getClass().getSimpleName()).start()) {
            return createInstance(inputStream, root);
        }
    }

    public TransmissionRequest newInstance(InputStream inputStream, Span root)
            throws IOException, OxalisTransmissionException {
        try (Span span = tracer.newChild(root.context()).name(getClass().getSimpleName()).start()) {
            return createInstance(inputStream, span);
        }
    }

    private TransmissionRequest createInstance(InputStream inputStream, Span root)
            throws IOException, OxalisTransmissionException {
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
                endpoint = lookupService.lookup(header, span);
                span.tag("transport profile", endpoint.getTransportProfile().getValue());
            } catch (OxalisTransmissionException e) {
                span.tag("exception", e.getMessage());
                throw e;
            }
        }

        // Create transmission request.
        return new DefaultTransmissionRequest(new MessageId(), header, peekingInputStream.newInputStream(), endpoint);
    }
}
