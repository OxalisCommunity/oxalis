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

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import io.opentracing.Span;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.header.HeaderParser;
import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.lookup.LookupService;
import network.oxalis.api.model.Direction;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.tag.Tag;
import network.oxalis.api.tag.TagGenerator;
import network.oxalis.api.transformer.ContentDetector;
import network.oxalis.sniffer.PeppolStandardBusinessHeader;
import network.oxalis.sniffer.identifier.InstanceId;
import network.oxalis.sniffer.sbdh.SbdhWrapper;
import network.oxalis.vefa.peppol.common.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author steinar
 * @author thore
 * Date: 04.11.13
 * Time: 10:04
 * @author erlend
 */
@Slf4j
public class TransmissionRequestBuilder {

    private final ContentDetector contentDetector;

    private final LookupService lookupService;

    private final TagGenerator tagGenerator;

    private final HeaderParser headerParser;

    private final Tracer tracer;

    private boolean allowOverride;

    /**
     * Will contain the payload PEPPOL document
     */
    private byte[] payload;

    /**
     * The address of the endpoint either supplied by the caller or looked up in the SMP
     */
    private Endpoint endpoint;

    private Tag tag = Tag.NONE;

    /**
     * The header fields supplied by the caller as opposed to the header fields parsed from the payload
     */
    private PeppolStandardBusinessHeader suppliedHeaderFields = new PeppolStandardBusinessHeader();

    /**
     * The header fields in effect, i.e. merge the parsed header fields with the supplied ones,
     * giving precedence to the supplied ones.
     */
    private PeppolStandardBusinessHeader effectiveStandardBusinessHeader;

    @Inject
    public TransmissionRequestBuilder(ContentDetector contentDetector, LookupService lookupService,
                                      TagGenerator tagGenerator, HeaderParser headerParser, Tracer tracer) {
        this.contentDetector = contentDetector;
        this.lookupService = lookupService;
        this.tagGenerator = tagGenerator;
        this.headerParser = headerParser;
        this.tracer = tracer;
    }

    public void reset() {
        suppliedHeaderFields = new PeppolStandardBusinessHeader();
        effectiveStandardBusinessHeader = null;
    }

    /**
     * Supplies the  builder with the contents of the message to be sent.
     */
    public TransmissionRequestBuilder payLoad(InputStream inputStream) {
        savePayLoad(inputStream);
        return this;
    }

    /**
     * Overrides the endpoint URL and the AS2 System identifier for the AS2 protocol.
     * You had better know what you are doing :-)
     */
    public TransmissionRequestBuilder overrideAs2Endpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public TransmissionRequestBuilder receiver(ParticipantIdentifier receiverId) {
        suppliedHeaderFields.setRecipientId(receiverId);
        return this;
    }

    public TransmissionRequestBuilder sender(ParticipantIdentifier senderId) {
        suppliedHeaderFields.setSenderId(senderId);
        return this;
    }

    public TransmissionRequestBuilder documentType(DocumentTypeIdentifier documentTypeIdentifier) {
        suppliedHeaderFields.setDocumentTypeIdentifier(documentTypeIdentifier);
        return this;
    }

    public TransmissionRequestBuilder processType(ProcessIdentifier processTypeId) {
        suppliedHeaderFields.setProfileTypeIdentifier(processTypeId);
        return this;
    }

    public TransmissionRequestBuilder c1CountryIdentifier(C1CountryIdentifier c1CountryIdentifier) {
        suppliedHeaderFields.setC1CountryIdentifier(c1CountryIdentifier);
        return this;
    }

    public TransmissionRequestBuilder instanceId(InstanceId instanceId) {
        suppliedHeaderFields.setInstanceId(instanceId);
        return this;
    }

    public TransmissionRequestBuilder tag(Tag tag) {
        this.tag = tag;
        return this;
    }

    public TransmissionRequest build(Span root) throws OxalisTransmissionException, OxalisContentException {
        Span span = tracer.buildSpan("build").asChildOf(root).start();
        try {
            return build();
        } finally {
            span.finish();
        }
    }

    /**
     * Builds the actual {@link TransmissionRequest}.
     * <p>
     * The  {@link PeppolStandardBusinessHeader} is built as following:
     *
     * <ol>
     * <li>If the payload contains an SBHD, allow override if global "overrideAllowed" flag is set,
     * otherwise use the one parsed</li>
     * <li>If the payload does not contain an SBDH, parseOld payload to determine some of the SBDH attributes
     * and allow override if global "overrideAllowed" flag is set.</li>
     * </ol>
     *
     * @return Prepared transmission request.
     */
    public TransmissionRequest build() throws OxalisTransmissionException, OxalisContentException {
        if (payload.length < 2)
            throw new OxalisTransmissionException("You have forgotten to provide payload");

        PeppolStandardBusinessHeader optionalParsedSbdh = null;
        try {
            optionalParsedSbdh =
                    new PeppolStandardBusinessHeader(headerParser.parse(new ByteArrayInputStream(payload)));
        } catch (OxalisContentException e) {
            // No action.
        }

        // Calculates the effectiveStandardBusinessHeader to be used
        effectiveStandardBusinessHeader = makeEffectiveSbdh(
                Optional.ofNullable(optionalParsedSbdh), suppliedHeaderFields);

        // If the endpoint has not been overridden by the caller, look up the endpoint address in
        // the SMP using the data supplied in the payload
        if (isEndpointSuppliedByCaller() && isOverrideAllowed()) {
            log.warn("Endpoint was set by caller not retrieved from SMP, make sure this is intended behaviour.");
        } else {
            Endpoint endpoint = lookupService.lookup(effectiveStandardBusinessHeader.toVefa(), null);

            if (isEndpointSuppliedByCaller() && !this.endpoint.equals(endpoint)) {
                throw new IllegalStateException("You are not allowed to override the EndpointAddress from SMP.");
            }

            this.endpoint = endpoint;
        }

        // make sure payload is encapsulated in SBDH
        if (optionalParsedSbdh == null) {
            // Wraps the payload with an SBDH, as this is required for AS2
            payload = wrapPayLoadWithSBDH(new ByteArrayInputStream(payload), effectiveStandardBusinessHeader);
        }

        // Transfers all the properties of this object into the newly created TransmissionRequest
        return new DefaultTransmissionRequest(
                getEffectiveStandardBusinessHeader().toVefa(), getPayload(),
                getEndpoint(), tagGenerator.generate(Direction.OUT, tag));
    }

    /**
     * Merges the SBDH parsed from the payload with the SBDH data supplied by the caller, i.e. the caller wishes to
     * override the contents of the SBDH parsed. That is, if the payload contains an SBDH
     *
     * @param optionalParsedSbdh         the SBDH as parsed (extracted) from the payload.
     * @param peppolSbdhSuppliedByCaller the SBDH data supplied by the caller in order to override data from the payload
     * @return the merged, effective SBDH created by combining the two data sets
     */
    PeppolStandardBusinessHeader makeEffectiveSbdh(Optional<PeppolStandardBusinessHeader> optionalParsedSbdh,
                                                   PeppolStandardBusinessHeader peppolSbdhSuppliedByCaller)
            throws OxalisContentException {
        PeppolStandardBusinessHeader effectiveSbdh;

        if (isOverrideAllowed()) {
            if (peppolSbdhSuppliedByCaller.isComplete()) {
                // we have sufficient meta data (set explicitly by the caller using API functions)
                effectiveSbdh = peppolSbdhSuppliedByCaller;
            } else {
                // missing meta data, parseOld payload, which does not contain SBHD, in order to deduce missing fields
                PeppolStandardBusinessHeader parsedPeppolStandardBusinessHeader = parsePayLoadAndDeduceSbdh(optionalParsedSbdh);
                effectiveSbdh = createEffectiveHeader(parsedPeppolStandardBusinessHeader, peppolSbdhSuppliedByCaller);
            }
        } else {
            // override is not allowed, make sure we do not override any restricted headers
            PeppolStandardBusinessHeader parsedPeppolStandardBusinessHeader = parsePayLoadAndDeduceSbdh(optionalParsedSbdh);
            List<String> overriddenHeaders = findRestricedHeadersThatWillBeOverridden(parsedPeppolStandardBusinessHeader, peppolSbdhSuppliedByCaller);
            if (overriddenHeaders.isEmpty()) {
                effectiveSbdh = createEffectiveHeader(parsedPeppolStandardBusinessHeader, peppolSbdhSuppliedByCaller);
            } else {
                throw new IllegalStateException("Your are not allowed to override " + Arrays.toString(overriddenHeaders.toArray()) + " in production mode, makes sure headers match the ones in the document.");
            }
        }
        if (!effectiveSbdh.isComplete()) {
            throw new IllegalStateException("TransmissionRequest can not be built, missing " + Arrays.toString(effectiveSbdh.listMissingProperties().toArray()) + " metadata.");
        }

        return effectiveSbdh;
    }


    private PeppolStandardBusinessHeader parsePayLoadAndDeduceSbdh(
            Optional<PeppolStandardBusinessHeader> optionallyParsedSbdh) throws OxalisContentException {
        if (optionallyParsedSbdh.isPresent())
            return optionallyParsedSbdh.get();

        return new PeppolStandardBusinessHeader(contentDetector.parse(new ByteArrayInputStream(payload)));
    }

    /**
     * Merges the supplied header fields with the SBDH parsed or derived from the payload thus allowing the caller
     * to explicitly override whatever has been supplied in the payload.
     *
     * @param parsed   the PeppolStandardBusinessHeader parsed from the payload
     * @param supplied the header fields supplied by the caller
     * @return the merged and effective headers
     */
    protected PeppolStandardBusinessHeader createEffectiveHeader(final PeppolStandardBusinessHeader parsed,
                                                                 final PeppolStandardBusinessHeader supplied) {

        // Creates a copy of the original business headers
        PeppolStandardBusinessHeader mergedHeaders = new PeppolStandardBusinessHeader(parsed);

        if (supplied.getSenderId() != null) {
            mergedHeaders.setSenderId(supplied.getSenderId());
        }
        if (supplied.getRecipientId() != null) {
            mergedHeaders.setRecipientId(supplied.getRecipientId());
        }
        if (supplied.getDocumentTypeIdentifier() != null) {
            mergedHeaders.setDocumentTypeIdentifier(supplied.getDocumentTypeIdentifier());
        }
        if (supplied.getProfileTypeIdentifier() != null) {
            mergedHeaders.setProfileTypeIdentifier(supplied.getProfileTypeIdentifier());
        }
        if (supplied.getC1CountryIdentifier() != null) {
            mergedHeaders.setC1CountryIdentifier(supplied.getC1CountryIdentifier());
        }

        // If instanceId was supplied by caller, use it otherwise, create new
        if (supplied.getInstanceId() != null) {
            mergedHeaders.setInstanceId(supplied.getInstanceId());
        } else {
            mergedHeaders.setInstanceId(new InstanceId());
        }

        if (supplied.getCreationDateAndTime() != null) {
            mergedHeaders.setCreationDateAndTime(supplied.getCreationDateAndTime());
        }

        return mergedHeaders;

    }

    /**
     * Returns a list of "restricted" header names that will be overridden when calling #createEffectiveHeader
     * The restricted header names are SenderId, RecipientId, DocumentTypeIdentifier and ProfileTypeIdentifier
     * Compares values that exist both as parsed and supplied headers.
     * Ignores values that only exists in one of them (that allows for sending new and unknown document types)
     */
    protected List<String> findRestricedHeadersThatWillBeOverridden(final PeppolStandardBusinessHeader parsed,
                                                                    final PeppolStandardBusinessHeader supplied) {
        List<String> headers = new ArrayList<>();
        if ((parsed.getSenderId() != null) && (supplied.getSenderId() != null)
                && (!supplied.getSenderId().equals(parsed.getSenderId()))) headers.add("SenderId");
        if ((parsed.getRecipientId() != null) && (supplied.getRecipientId() != null)
                && (!supplied.getRecipientId().equals(parsed.getRecipientId()))) headers.add("RecipientId");
        if ((parsed.getDocumentTypeIdentifier() != null) && (supplied.getDocumentTypeIdentifier() != null)
                && (!supplied.getDocumentTypeIdentifier().equals(parsed.getDocumentTypeIdentifier())))
            headers.add("DocumentTypeIdentifier");
        if ((parsed.getProfileTypeIdentifier() != null) && (supplied.getProfileTypeIdentifier() != null)
                && (!supplied.getProfileTypeIdentifier().equals(parsed.getProfileTypeIdentifier())))
            headers.add("ProfileTypeIdentifier");
        return headers;
    }

    protected PeppolStandardBusinessHeader getEffectiveStandardBusinessHeader() {
        return effectiveStandardBusinessHeader;
    }

    protected void savePayLoad(InputStream inputStream) {
        try {
            payload = ByteStreams.toByteArray(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save the payload: " + e.getMessage(), e);
        }
    }

    protected InputStream getPayload() {
        return new ByteArrayInputStream(payload);
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public boolean isOverrideAllowed() {
        return allowOverride;
    }

    private boolean isEndpointSuppliedByCaller() {
        return endpoint != null;
    }

    private byte[] wrapPayLoadWithSBDH(ByteArrayInputStream byteArrayInputStream,
                                       PeppolStandardBusinessHeader effectiveStandardBusinessHeader) {
        SbdhWrapper sbdhWrapper = new SbdhWrapper();
        return sbdhWrapper.wrap(byteArrayInputStream, effectiveStandardBusinessHeader.toVefa());
    }

    /**
     * For testing purposes only
     */
    public void setTransmissionBuilderOverride(boolean transmissionBuilderOverride) {
        this.allowOverride = transmissionBuilderOverride;
    }
}
