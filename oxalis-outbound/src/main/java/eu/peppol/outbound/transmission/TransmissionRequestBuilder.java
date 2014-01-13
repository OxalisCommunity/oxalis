package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.DocumentSniffer;
import eu.peppol.document.NoSbdhParser;
import eu.peppol.document.SbdhParser;
import eu.peppol.document.SbdhWrapper;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.security.CommonName;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author steinar
 *         Date: 04.11.13
 *         Time: 10:04
 */
public class TransmissionRequestBuilder {

    public static final Logger log = LoggerFactory.getLogger(TransmissionRequestBuilder.class);

    final SbdhParser sbdhParser;
    final NoSbdhParser noSbdhParser;
    final SmpLookupManager smpLookupManager;


    private byte[] payload;

    /**
     * The address of the endpoint either supplied by the caller or looked up in the SMP
     */
    private SmpLookupManager.PeppolEndpointData endpointAddress;

    /** The header fields supplied by the caller as opposed to the header fields parsed from the payload */
    private SuppliedHeaderFields suppliedHeaderFields = new SuppliedHeaderFields();

    /** The header fields in effect, i.e. merge the parsed header fields into the supplied ones, giving precedence to the supplied ones. */
    private PeppolStandardBusinessHeader effectiveStandardBusinessHeader;

    /**
     * Indicates whether the payload contains an SBDH or not, which is determined by sniffing at the document before parsing it
     */
    private boolean sbdhDetected;

    /**
     * Supplied by the caller
     */
    private ParticipantId receiverId;

    @Inject
    public TransmissionRequestBuilder(SbdhParser sbdhParser, NoSbdhParser noSbdhParser, SmpLookupManager smpLookupManager) {
        this.sbdhParser = sbdhParser;
        this.noSbdhParser = noSbdhParser;
        this.smpLookupManager = smpLookupManager;
    }

    /**
     * Supplies the  builder with the contents of the message to be sent.
     *
     * @param inputStream
     * @return
     */
    public TransmissionRequestBuilder payLoad(InputStream inputStream) {

        savePayLoad(inputStream);

        return this;
    }

    /**
     * Overrides the endpoint URL for the START transmission protocol.
     */
    public TransmissionRequestBuilder overrideEndpointForStartProtocol(URL url) {
        endpointAddress = new SmpLookupManager.PeppolEndpointData(url, BusDoxProtocol.START);
        return this;
    }

    /**
     * Overrides the endpoint URL and the AS2 System identifier for the AS2 protocol.
     * You had better know what you are doing :-)
     */
    public TransmissionRequestBuilder overrideAs2Endpoint(URL url, String accessPointSystemIdentifier) {
        endpointAddress = new SmpLookupManager.PeppolEndpointData(url, BusDoxProtocol.AS2, new CommonName(accessPointSystemIdentifier));
        return this;
    }

    public TransmissionRequestBuilder receiver(ParticipantId receiverId) {
        suppliedHeaderFields.receiver = receiverId;
        return this;
    }

    public TransmissionRequestBuilder sender(ParticipantId senderId) {
        suppliedHeaderFields.sender = senderId;
        return this;

    }

    public TransmissionRequestBuilder documentType(PeppolDocumentTypeId documentTypeId) {
        suppliedHeaderFields.documentTypeId = documentTypeId;
        return this;
    }

    public TransmissionRequestBuilder processType(PeppolProcessTypeId processTypeId) {
        suppliedHeaderFields.processTypeId = processTypeId;
        return this;
    }

    public TransmissionRequest build() {

        PeppolStandardBusinessHeader parsedPeppolStandardBusinessHeader = parsePayLoadAndDeduceSbdh();

        effectiveStandardBusinessHeader = createEffectiveHeader(parsedPeppolStandardBusinessHeader, suppliedHeaderFields);

        // If the endpoint has not been overridden by the caller, look up the endpoint address in the SMP using the data supplied in the payload
        if (!isEndpointOverridden()) {

            endpointAddress = smpLookupManager.getEndpointTransmissionData(effectiveStandardBusinessHeader.getRecipientId(), effectiveStandardBusinessHeader.getDocumentTypeIdentifier());

        }

        if (endpointAddress.getBusDoxProtocol() == BusDoxProtocol.AS2 && !sbdhDetected) {

            // Wraps the payload with an SBDH, as this is required for AS2
            payload = wrapPayLoadWithSBDH(new ByteArrayInputStream(payload));

        } else if (endpointAddress.getBusDoxProtocol() == BusDoxProtocol.START && sbdhDetected) {
                throw new IllegalStateException("Payload may not contain SBDH when using protocol " + endpointAddress.getBusDoxProtocol().toString());
        }

        // Transfers all the properties of this object into the newly created TransmissionRequest
        return new TransmissionRequest(this);

    }

    /**
     * Merges the supplied header fields into the SBDH parsed from the payload thus allowing the caller to explicitly override whatever
     * has been supplied in the payload.
     *
     * @param parsedSbdh the PeppolStandardBusinessHeader parsed from the payload
     * @param supplied the header fields supplied by the caller
     * @return
     */
    protected PeppolStandardBusinessHeader createEffectiveHeader(final PeppolStandardBusinessHeader parsedSbdh, SuppliedHeaderFields supplied) {

        // Creates a copy of the original business headers
        PeppolStandardBusinessHeader mergedHeaders = new PeppolStandardBusinessHeader(parsedSbdh);

        if (supplied.sender != null) {
            mergedHeaders.setSenderId(supplied.sender);
        }
        if (supplied.receiver != null) {
            mergedHeaders.setRecipientId(supplied.receiver);
        }
        if (supplied.documentTypeId != null) {
            mergedHeaders.setDocumentTypeIdentifier(supplied.documentTypeId);
        }
        if (supplied.processTypeId != null) {
            mergedHeaders.setProfileTypeIdentifier(supplied.processTypeId);
        }

        return mergedHeaders;
    }


    protected boolean isEndpointOverridden() {
        return endpointAddress != null;
    }

    PeppolStandardBusinessHeader parsePayLoadAndDeduceSbdh() {
        sbdhDetected = checkForSbdh();

        PeppolStandardBusinessHeader peppolSbdh;
        if (sbdhDetected) {
            // Parses the SBDH to determine the receivers endpoint URL etc.
            peppolSbdh = sbdhParser.parse(new ByteArrayInputStream(payload));
        } else {
            // Parses the PEPPOL document in order to determine the header fields
            peppolSbdh = noSbdhParser.parse(new ByteArrayInputStream(payload));
        }

        return peppolSbdh;
    }

    boolean checkForSbdh() {
        // Sniff, sniff; does it contain a SBDH?
        DocumentSniffer documentSniffer = new DocumentSniffer(new ByteArrayInputStream(payload));
        return documentSniffer.isSbdhDetected();
    }

    void savePayLoad(InputStream inputStream) {
        try {
            payload = Util.intoBuffer(inputStream, 6L * 1024 * 1024);     // Copies the contents into a buffer
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save the payload: " + e.getMessage(), e);
        }
    }


    PeppolStandardBusinessHeader getEffectiveStandardBusinessHeader() {
        return effectiveStandardBusinessHeader;
    }

    byte[] getPayload() {
        return payload;
    }

    SmpLookupManager.PeppolEndpointData getEndpointAddress() {
        return endpointAddress;
    }

    private byte[] wrapPayLoadWithSBDH(ByteArrayInputStream byteArrayInputStream) {
        SbdhWrapper sbdhWrapper = new SbdhWrapper();
        byte[] result = sbdhWrapper.wrap(byteArrayInputStream);

        return result;
    }

    static class SuppliedHeaderFields {
        ParticipantId sender;
        ParticipantId receiver;
        PeppolDocumentTypeId documentTypeId;
        PeppolProcessTypeId processTypeId;
    }
}
