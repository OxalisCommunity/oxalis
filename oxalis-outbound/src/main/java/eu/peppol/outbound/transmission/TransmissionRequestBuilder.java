/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.outbound.transmission;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.NoSbdhParser;
import eu.peppol.document.Sbdh2PeppolHeaderConverter;
import eu.peppol.document.SbdhFastParser;
import eu.peppol.document.SbdhWrapper;
import eu.peppol.identifier.*;
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.security.CommonName;
import eu.peppol.smp.PeppolEndpointData;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.util.GlobalConfiguration;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author steinar
 * @author thore
 *         Date: 04.11.13
 *         Time: 10:04
 */
public class TransmissionRequestBuilder {

    private static final Logger log = LoggerFactory.getLogger(TransmissionRequestBuilder.class);

    private final NoSbdhParser noSbdhParser;

    private final SmpLookupManager smpLookupManager;

    private final GlobalConfiguration globalConfiguration;

    /**
     * Will contain the payload PEPPOL document
     */
    private byte[] payload;

    /**
     * Holds the MessageId used as the value in the {@link eu.peppol.as2.As2Header#MESSAGE_ID}
     */
    private MessageId messageId;

    /**
     * The address of the endpoint either supplied by the caller or looked up in the SMP
     */
    private PeppolEndpointData endpointAddress;

    private no.difi.vefa.peppol.common.model.Endpoint endpoint;

    /**
     * The header fields supplied by the caller as opposed to the header fields parsed from the payload
     */
    private PeppolStandardBusinessHeader suppliedHeaderFields = new PeppolStandardBusinessHeader();

    /**
     * The header fields in effect, i.e. merge the parsed header fields with the supplied ones, giving precedence to the supplied ones.
     */
    private PeppolStandardBusinessHeader effectiveStandardBusinessHeader;

    @Inject
    public TransmissionRequestBuilder(NoSbdhParser noSbdhParser, SmpLookupManager smpLookupManager, GlobalConfiguration globalConfiguration) {
        this.noSbdhParser = noSbdhParser;
        this.smpLookupManager = smpLookupManager;

        this.globalConfiguration = globalConfiguration;
        log.debug("GlobalConfiguration implementation: " + globalConfiguration);
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
    public TransmissionRequestBuilder overrideAs2Endpoint(URI url, String accessPointSystemIdentifier) {
        endpointAddress = new PeppolEndpointData(url, BusDoxProtocol.AS2, (accessPointSystemIdentifier == null) ? null : new CommonName(accessPointSystemIdentifier));
        return this;
    }

    public TransmissionRequestBuilder receiver(ParticipantId receiverId) {
        suppliedHeaderFields.setRecipientId(receiverId);
        return this;
    }

    public TransmissionRequestBuilder sender(ParticipantId senderId) {
        suppliedHeaderFields.setSenderId(senderId);
        return this;
    }

    public TransmissionRequestBuilder documentType(PeppolDocumentTypeId documentTypeId) {
        suppliedHeaderFields.setDocumentTypeIdentifier(documentTypeId);
        return this;
    }

    public TransmissionRequestBuilder processType(PeppolProcessTypeId processTypeId) {
        suppliedHeaderFields.setProfileTypeIdentifier(processTypeId);
        return this;
    }

    public TransmissionRequestBuilder instanceId(InstanceId instanceId) {
        suppliedHeaderFields.setInstanceId(instanceId);
        return this;
    }

    public TransmissionRequestBuilder messageId(MessageId messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Builds the actual {@link TransmissionRequest}.
     * <p>
     * The  {@link PeppolStandardBusinessHeader} is built as following:
     * <p>
     * <ol>
     * <li>If the payload contains an SBHD, allow override if global "overrideAllowed" flag is set, otherwise use the one parsed</li>
     * <li>If the payload does not contain an SBDH, parse payload to determine some of the SBDH attributes and allow override if global "overrideAllowed" flag is set.</li>
     * </ol>
     *
     * @return Prepared transmission request.
     */
    public TransmissionRequest build() throws OxalisTransmissionException {
        if (payload.length < 2)
            throw new OxalisTransmissionException("You have forgotten to provide payload");

        SbdhFastParser sbdhFastParser = new SbdhFastParser();
        Optional<StandardBusinessDocumentHeader> optionalParsedSbdh = Optional.ofNullable(sbdhFastParser.parse(new ByteArrayInputStream(payload)));

        // Calculates the effectiveStandardBusinessHeader to be used
        effectiveStandardBusinessHeader = makeEffectiveSbdh(optionalParsedSbdh, suppliedHeaderFields);

        // If the endpoint has not been overridden by the caller, look up the endpoint address in the SMP using the data supplied in the payload
        if (isEndpointSuppliedByCaller() && isOverrideAllowed()) {
            log.warn("Endpoint was set by caller not retrieved from SMP, make sure this is intended behaviour.");
        } else {
            PeppolEndpointData lookupEndpointAddress = smpLookupManager.getEndpointTransmissionData(effectiveStandardBusinessHeader.getRecipientId(), effectiveStandardBusinessHeader.getDocumentTypeIdentifier());
            if (isEndpointSuppliedByCaller() && !endpointAddress.equals(lookupEndpointAddress)) {
                throw new IllegalStateException("You are not allowed to override the EndpointAddress from SMP in production mode.");
            }
            this.endpointAddress = lookupEndpointAddress;
        }

        // make sure payload is encapsulated in SBDH
        if (!optionalParsedSbdh.isPresent()) {
            // Wraps the payload with an SBDH, as this is required for AS2
            payload = wrapPayLoadWithSBDH(new ByteArrayInputStream(payload), effectiveStandardBusinessHeader);
        }

        if (messageId == null) {
            messageId = new MessageId();
            log.info("TransmissionRequest was assigned messageId:" + messageId);
        }

        // Transfers all the properties of this object into the newly created TransmissionRequest
        return new DefaultTransmissionRequest(this);
    }

    /**
     * Merges the SBDH parsed from the payload with the SBDH data supplied by the caller, i.e. the caller wishes to
     * override the contents of the SBDH parsed. That is, if the payload contains an SBDH
     *
     * @param optionalParsedSbdh         the SBDH as parsed (extracted) from the payload.
     * @param peppolSbdhSuppliedByCaller the SBDH data supplied by the caller in order to override data from the payload
     * @return the merged, effective SBDH created by combining the two data sets
     */
    PeppolStandardBusinessHeader makeEffectiveSbdh(Optional<StandardBusinessDocumentHeader> optionalParsedSbdh, PeppolStandardBusinessHeader peppolSbdhSuppliedByCaller) {
        PeppolStandardBusinessHeader effectiveSbdh;

        if (isOverrideAllowed()) {
            if (peppolSbdhSuppliedByCaller.isComplete()) {
                // we have sufficient meta data (set explicitly by the caller using API functions)
                effectiveSbdh = peppolSbdhSuppliedByCaller;
            } else {
                // missing meta data, parse payload, which does not contain SBHD, in order to deduce missing fields
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


    private PeppolStandardBusinessHeader parsePayLoadAndDeduceSbdh(Optional<StandardBusinessDocumentHeader> optionallyParsedSbdh) {
        PeppolStandardBusinessHeader peppolSbdh;

        // If an SBDH was parsed from the payload, use it
        if (optionallyParsedSbdh.isPresent()) {
            peppolSbdh = Sbdh2PeppolHeaderConverter.convertSbdh2PeppolHeader(optionallyParsedSbdh.get());
        } else {
            // otherwise parses the payload and creates a PEPPOL SBDH from the contents of the payload.
            peppolSbdh = noSbdhParser.parse(new ByteArrayInputStream(payload));
        }
        return peppolSbdh;
    }

    /**
     * Merges the supplied header fields with the SBDH parsed or derived from the payload thus allowing the caller
     * to explicitly override whatever has been supplied in the payload.
     *
     * @param parsed   the PeppolStandardBusinessHeader parsed from the payload
     * @param supplied the header fields supplied by the caller
     * @return the merged and effective headers
     */
    protected PeppolStandardBusinessHeader createEffectiveHeader(final PeppolStandardBusinessHeader parsed, final PeppolStandardBusinessHeader supplied) {

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
    protected List<String> findRestricedHeadersThatWillBeOverridden(final PeppolStandardBusinessHeader parsed, final PeppolStandardBusinessHeader supplied) {
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

    public MessageId getMessageId() {
        return messageId;
    }

    protected PeppolEndpointData getEndpointAddress() {
        return endpointAddress;
    }

    public no.difi.vefa.peppol.common.model.Endpoint getEndpoint() {
        return endpoint;
    }

    public boolean isOverrideAllowed() {
        return globalConfiguration.isTransmissionBuilderOverride();
    }

    private boolean isEndpointSuppliedByCaller() {
        return endpointAddress != null;
    }

    private byte[] wrapPayLoadWithSBDH(ByteArrayInputStream byteArrayInputStream, PeppolStandardBusinessHeader effectiveStandardBusinessHeader) {
        SbdhWrapper sbdhWrapper = new SbdhWrapper();
        return sbdhWrapper.wrap(byteArrayInputStream, effectiveStandardBusinessHeader);
    }

    /**
     * For testing purposes only
     */
    void setTransmissionBuilderOverride(boolean transmissionBuilderOverride) {
        globalConfiguration.setTransmissionBuilderOverride(transmissionBuilderOverride);
    }
}
