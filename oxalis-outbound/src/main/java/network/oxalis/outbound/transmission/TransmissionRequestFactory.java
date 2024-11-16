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

package network.oxalis.outbound.transmission;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import network.oxalis.api.header.HeaderParser;
import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.api.model.Direction;
import network.oxalis.api.outbound.TransmissionMessage;
import network.oxalis.api.tag.Tag;
import network.oxalis.api.tag.TagGenerator;
import network.oxalis.api.transformer.ContentDetector;
import network.oxalis.api.transformer.ContentWrapper;
import network.oxalis.commons.io.PeekingInputStream;
import network.oxalis.commons.tracing.Traceable;
import network.oxalis.vefa.peppol.common.model.Header;

import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author erlend
 * @since 4.0.0
 */
public class TransmissionRequestFactory extends Traceable {

    private final ContentDetector contentDetector;

    private final ContentWrapper contentWrapper;

    private final TagGenerator tagGenerator;

    private final HeaderParser headerParser;

    @Inject
    public TransmissionRequestFactory(ContentDetector contentDetector, ContentWrapper contentWrapper,
                                      TagGenerator tagGenerator, HeaderParser headerParser, Tracer tracer) {
        super(tracer);
        this.contentDetector = contentDetector;
        this.contentWrapper = contentWrapper;
        this.tagGenerator = tagGenerator;
        this.headerParser = headerParser;
    }

    public TransmissionMessage newInstance(InputStream inputStream)
            throws IOException, OxalisContentException {
        return newInstance(inputStream, Tag.NONE);
    }

    public TransmissionMessage newInstance(InputStream inputStream, Tag tag)
            throws IOException, OxalisContentException {
        Span span = tracer.spanBuilder(getClass().getSimpleName()).startSpan();
        try {
            return perform(inputStream, tag);
        } finally {
            span.end();
        }
    }

    private TransmissionMessage perform(InputStream inputStream, Tag tag)
            throws IOException, OxalisContentException {
        PeekingInputStream peekingInputStream = new PeekingInputStream(inputStream);
        try {
            Header header = readHeaderFromSbdh(peekingInputStream);
            return new DefaultTransmissionMessage(header, peekingInputStream.newInputStream(),
                    tagGenerator.generate(Direction.OUT, tag));
        } catch (OxalisContentException e) {
            byte[] payload = peekingInputStream.getContent();
            Header header = detectHeaderFromContent(payload);
            InputStream wrappedContent = wrapContentInSbdh(header, payload);
            return new DefaultTransmissionMessage(header, wrappedContent, tagGenerator.generate(Direction.OUT, tag));
        }
    }

    private Header readHeaderFromSbdh(PeekingInputStream peekingInputStream) throws OxalisContentException {
        Span span = tracer.spanBuilder("Reading SBDH").startSpan();
        try {
            Header header = headerParser.parse(peekingInputStream);
            span.setAttribute("identifier", header.getIdentifier().getIdentifier());
            return header;
        } catch (OxalisContentException e) {
            span.setAttribute("exception", e.getMessage());
            throw e;
        } finally {
            span.end();
        }

    }

    private Header detectHeaderFromContent(byte[] payload) throws OxalisContentException {
        Span span = tracer.spanBuilder("Detect SBDH from content").startSpan();
        try {
            Header header = contentDetector.parse(new ByteArrayInputStream(payload));
            span.setAttribute("identifier", header.getIdentifier().getIdentifier());
            return header;
        } catch (OxalisContentException e) {
            span.setAttribute("exception", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    private InputStream wrapContentInSbdh(Header header, byte[] payload) throws IOException, OxalisContentException {
        Span span = tracer.spanBuilder("Wrap content in SBDH").startSpan();
        try {
            return contentWrapper.wrap(new ByteArrayInputStream(payload), header);
        } catch (OxalisContentException e) {
            span.setAttribute("exception", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
