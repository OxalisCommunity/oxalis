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

import io.opentracing.Span;
import io.opentracing.Tracer;
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

import javax.inject.Inject;
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
        Span root = tracer.buildSpan(getClass().getSimpleName()).start();
        try {
            return perform(inputStream, tag, root);
        } finally {
            root.finish();
        }
    }

    public TransmissionMessage newInstance(InputStream inputStream, Span root)
            throws IOException, OxalisContentException {
        return newInstance(inputStream, Tag.NONE, root);
    }

    public TransmissionMessage newInstance(InputStream inputStream, Tag tag, Span root)
            throws IOException, OxalisContentException {
        Span span = tracer.buildSpan(getClass().getSimpleName()).asChildOf(root).start();
        try {
            return perform(inputStream, tag, span);
        } finally {
            span.finish();
        }
    }

    private TransmissionMessage perform(InputStream inputStream, Tag tag, Span root)
            throws IOException, OxalisContentException {
        PeekingInputStream peekingInputStream = new PeekingInputStream(inputStream);

        // Read header from content to send.
        Header header;
        try {
            // Read header from SBDH.
            Span span = tracer.buildSpan("Reading SBDH").asChildOf(root).start();
            try {
                header = headerParser.parse(peekingInputStream);
                span.setTag("identifier", header.getIdentifier().getIdentifier());
            } catch (OxalisContentException e) {
                span.setTag("exception", e.getMessage());
                throw e;
            } finally {
                span.finish();
            }

            // Create transmission request.
            return new DefaultTransmissionMessage(header, peekingInputStream.newInputStream(),
                    tagGenerator.generate(Direction.OUT, tag));
        } catch (OxalisContentException e) {
            byte[] payload = peekingInputStream.getContent();

            // Detect header from content.
            Span span = tracer.buildSpan("Detect SBDH from content").asChildOf(root).start();
            try {
                header = contentDetector.parse(new ByteArrayInputStream(payload));
                span.setTag("identifier", header.getIdentifier().getIdentifier());
            } catch (OxalisContentException ex) {
                span.setTag("exception", ex.getMessage());
                throw new OxalisContentException(ex.getMessage(), ex);
            } finally {
                span.finish();
            }

            // Wrap content in SBDH.
            span = tracer.buildSpan("Wrap content in SBDH").asChildOf(root).start();
            InputStream wrappedContent;
            try {
                wrappedContent = contentWrapper.wrap(new ByteArrayInputStream(payload), header);
            } catch (OxalisContentException ex) {
                span.setTag("exception", ex.getMessage());
                throw ex;
            } finally {
                span.finish();
            }

            // Create transmission request.
            return new DefaultTransmissionMessage(header, wrappedContent, tagGenerator.generate(Direction.OUT, tag));
        }
    }
}
