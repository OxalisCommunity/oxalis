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

import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.*;
import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.security.CommonName;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author steinar
 * @author thore
 *         Date: 04.11.13
 *         Time: 10:04
 */
public class TransmissionRequestBuilder {

    private static final Logger log = LoggerFactory.getLogger(TransmissionRequestBuilder.class);

    private final Sbdh2PeppolHeaderParser sbdh2PeppolHeaderParser;
    private final NoSbdhParser noSbdhParser;
    private final SmpLookupManager smpLookupManager;
    private final GlobalConfiguration globalConfiguration;

    /**
     * When enabled, also logs the payload handled
     */
    private boolean traceEnabled;

    /**
     * Will contain the payload PEPPOL document
     */
    private byte[] payload;

    /**
     * The address of the endpoint either supplied by the caller or looked up in the SMP
     */
    private SmpLookupManager.PeppolEndpointData endpointAddress;

    /**
     * The header fields supplied by the caller as opposed to the header fields parsed from the payload
     * */
    private PeppolStandardBusinessHeader suppliedHeaderFields = new PeppolStandardBusinessHeader();

    /**
     * The header fields in effect, i.e. merge the parsed header fields with the supplied ones, giving precedence to the supplied ones.
     */
    private PeppolStandardBusinessHeader effectiveStandardBusinessHeader;

    @Inject
    public TransmissionRequestBuilder(Sbdh2PeppolHeaderParser sbdh2PeppolHeaderParser, NoSbdhParser noSbdhParser, SmpLookupManager smpLookupManager, GlobalConfiguration globalConfiguration) {
        this.sbdh2PeppolHeaderParser = sbdh2PeppolHeaderParser;
        this.noSbdhParser = noSbdhParser;
        this.smpLookupManager = smpLookupManager;

        this.globalConfiguration = globalConfiguration;
        log.debug("GlobalConfiguration implementation: " + globalConfiguration);
    }


    public void reset() {
        suppliedHeaderFields = new PeppolStandardBusinessHeader();
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
    public TransmissionRequestBuilder overrideAs2Endpoint(URL url, String accessPointSystemIdentifier) {
        endpointAddress = new SmpLookupManager.PeppolEndpointData(url, BusDoxProtocol.AS2, (accessPointSystemIdentifier == null) ? null : new CommonName(accessPointSystemIdentifier));
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

    public TransmissionRequestBuilder messageId(MessageId messageId) {
        suppliedHeaderFields.setMessageId(messageId);
        return this;
    }

    public TransmissionRequestBuilder trace(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
        return this;
    }

    public TransmissionRequest build() {

        // Parses the SBDH of the payload, if it exists.
        SbdhFastParser sbdhFastParser = new SbdhFastParser();
        StandardBusinessDocumentHeader parsedSbdh = sbdhFastParser.parse(new ByteArrayInputStream(payload));

        // Calculates the effectiveStandardBusinessHeader to be used
        effectiveStandardBusinessHeader = makeEffectiveSbdh(parsedSbdh, suppliedHeaderFields);

        // If the endpoint has not been overridden by the caller, look up the endpoint address in the SMP using the data supplied in the payload
        if (isEndpointSuppliedByCaller() && isOverrideAllowed()) {
            log.warn("Endpoint was set by caller not retrieved from SMP, make sure this is intended behaviour.");
        } else {
            SmpLookupManager.PeppolEndpointData lookupEndpointAddress = smpLookupManager.getEndpointTransmissionData(effectiveStandardBusinessHeader.getRecipientId(), effectiveStandardBusinessHeader.getDocumentTypeIdentifier());
            if (isEndpointSuppliedByCaller() && !endpointAddress.equals(lookupEndpointAddress)) {
                throw new IllegalStateException("You are not allowed to override the EndpointAddress from SMP in production mode.");
            }
            endpointAddress = lookupEndpointAddress;
        }

        // make sure payload is encapsulated in SBDH for AS2 protocol
        if (BusDoxProtocol.AS2.equals(endpointAddress.getBusDoxProtocol())  && (parsedSbdh == null)) {
            // Wraps the payload with an SBDH, as this is required for AS2
            payload = wrapPayLoadWithSBDH(new ByteArrayInputStream(payload), effectiveStandardBusinessHeader);
        }

        if (isTraceEnabled()) {
            log.debug("This payload was built\n" + new String(payload));
        }

        // Transfers all the properties of this object into the newly created TransmissionRequest
        return new TransmissionRequest(this);
    }

    /**
     * Merges the SBDH parsed from the payload with the SBDH data supplied by the caller, i.e. the caller wishes to
     * override the contents of the SBDH parsed.
     *
     * @param parsedSbdh the SBDH as parsed (extracted) from the payload.
     * @param peppolSbdhSuppliedByCaller the SBDH data supplied by the caller in order to override data from the payload
     * @return the merged, effective SBDH created by combining the two data sets
     */
    PeppolStandardBusinessHeader makeEffectiveSbdh(StandardBusinessDocumentHeader parsedSbdh, PeppolStandardBusinessHeader peppolSbdhSuppliedByCaller) {
        PeppolStandardBusinessHeader peppolSbdh = null;

        if (isOverrideAllowed()) {
            if (peppolSbdhSuppliedByCaller.isComplete()) {
                // we have sufficient meta data (set explicitly by the caller using API functions)
                peppolSbdh = peppolSbdhSuppliedByCaller;
            } else {
                // missing meta data, parse payload, which does not contain SBHD, in order to deduce missing fields
                PeppolStandardBusinessHeader parsedPeppolStandardBusinessHeader = parsePayLoadAndDeduceSbdh(parsedSbdh);
                peppolSbdh = createEffectiveHeader(parsedPeppolStandardBusinessHeader, peppolSbdhSuppliedByCaller);
            }
        } else {
            // override is not allowed, make sure we do not override any restricted headers
            PeppolStandardBusinessHeader parsedPeppolStandardBusinessHeader = parsePayLoadAndDeduceSbdh(parsedSbdh);
            List<String> overriddenHeaders = findRestricedHeadersThatWillBeOverridden(parsedPeppolStandardBusinessHeader, peppolSbdhSuppliedByCaller);
            if (overriddenHeaders.isEmpty()) {
                peppolSbdh = createEffectiveHeader(parsedPeppolStandardBusinessHeader, peppolSbdhSuppliedByCaller);
            } else {
                throw new IllegalStateException("Your are not allowed to override " + Arrays.toString(overriddenHeaders.toArray()) + " in production mode, makes sure headers match the ones in the document.");
            }
        }
        if (!peppolSbdh.isComplete()) {
            throw new IllegalStateException("TransmissionRequest can not be built, missing " + Arrays.toString(peppolSbdh.listMissingProperties().toArray()) + " metadata.");
        }

        return peppolSbdh;
    }


    private PeppolStandardBusinessHeader parsePayLoadAndDeduceSbdh(StandardBusinessDocumentHeader parsedSbdh) {
        PeppolStandardBusinessHeader peppolSbdh;

        // If an SBDH was parsed from the payload, use it
        if (parsedSbdh != null) {
            peppolSbdh = Sbdh2PeppolHeaderConverter.convertSbdh2PeppolHeader(parsedSbdh);
        } else {
            // Parses the BIS document, which is not wrapped in an SBD(H) in order to determine the header fields
            peppolSbdh = noSbdhParser.parse(new ByteArrayInputStream(payload));
        }
        return peppolSbdh;
    }

    /**
     * Merges the supplied header fields with the SBDH parsed from the payload thus allowing the caller
     * to explicitly override whatever has been supplied in the payload.
     *
     * @param parsed the PeppolStandardBusinessHeader parsed from the payload
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
        if (supplied.getMessageId() != null) {
            mergedHeaders.setMessageId(supplied.getMessageId());
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
        List<String> headers = new ArrayList<String>();
        if ((parsed.getSenderId() != null) && (supplied.getSenderId() != null)
                && (!supplied.getSenderId().equals(parsed.getSenderId()))) headers.add("SenderId");
        if ((parsed.getRecipientId() != null) && (supplied.getRecipientId() != null)
                && (!supplied.getRecipientId().equals(parsed.getRecipientId()))) headers.add("RecipientId");
        if ((parsed.getDocumentTypeIdentifier() != null) && (supplied.getDocumentTypeIdentifier() != null)
                && (!supplied.getDocumentTypeIdentifier().equals(parsed.getDocumentTypeIdentifier()))) headers.add("DocumentTypeIdentifier");
        if ((parsed.getProfileTypeIdentifier() != null) && (supplied.getProfileTypeIdentifier() != null)
                && (!supplied.getProfileTypeIdentifier().equals(parsed.getProfileTypeIdentifier()))) headers.add("ProfileTypeIdentifier");
        return headers;
    }

    protected PeppolStandardBusinessHeader getEffectiveStandardBusinessHeader() {
        return effectiveStandardBusinessHeader;
    }

    protected void savePayLoad(InputStream inputStream) {
        try {
            long maxBytes = 101L * 1024 * 1024;
            payload = Util.intoBuffer(inputStream, maxBytes);     // Copies the contents into a buffer
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save the payload: " + e.getMessage(), e);
        }
    }

    protected byte[] getPayload() {
        return payload;
    }

    protected SmpLookupManager.PeppolEndpointData getEndpointAddress() {
        return endpointAddress;
    }

    public boolean isOverrideAllowed() {
        return globalConfiguration.isTransmissionBuilderOverride();
    }

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    private boolean isEndpointSuppliedByCaller() {
        return endpointAddress != null;
    }

    private byte[] wrapPayLoadWithSBDH(ByteArrayInputStream byteArrayInputStream, PeppolStandardBusinessHeader effectiveStandardBusinessHeader) {
        SbdhWrapper sbdhWrapper = new SbdhWrapper();
        return sbdhWrapper.wrap(byteArrayInputStream, effectiveStandardBusinessHeader);
    }

    /** For testing purposes only */
    void setTransmissionBuilderOverride(boolean transmissionBuilderOverride) {
        globalConfiguration.setTransmissionBuilderOverride(transmissionBuilderOverride);
    }
}
