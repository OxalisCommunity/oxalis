package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.DocumentSniffer;
import eu.peppol.document.NoSbdhParser;
import eu.peppol.document.SbdhParser;
import eu.peppol.document.SbdhWrapper;
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

    private final NoSbdhParser noSbdhParser;

    private final SmpLookupManager smpLookupManager;


    private byte[] payload;
    private PeppolStandardBusinessHeader peppolStandardBusinessHeader;
    private SmpLookupManager.PeppolEndpointData endpointAddress;

    private boolean sbdhDetected;

    @Inject
    public TransmissionRequestBuilder(SbdhParser sbdhParser, NoSbdhParser noSbdhParser, SmpLookupManager smpLookupManager) {
        this.sbdhParser = sbdhParser;
        this.noSbdhParser = noSbdhParser;
        this.smpLookupManager = smpLookupManager;
    }

    public TransmissionRequestBuilder payLoad(InputStream inputStream) {

        savePayLoad(inputStream);


        return this;
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

    /**
     * Overrides the endpoint URL for the START transmission protocol.
     */
    public TransmissionRequestBuilder overrideStartEndpoint(URL url) {
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

    PeppolStandardBusinessHeader getPeppolStandardBusinessHeader() {
        return peppolStandardBusinessHeader;
    }

    byte[] getPayload() {
        return payload;
    }

    SmpLookupManager.PeppolEndpointData getEndpointAddress() {
        return endpointAddress;
    }


    public TransmissionRequest build() {

        peppolStandardBusinessHeader = parsePayLoadAndDeduceSbdh();

        // were do we send this stuff? Lookup in SMP, unless caller has directly overridden with another end point
        if (endpointAddress == null) {
            // TODO: must search for the optimal transport protocol, i.e. prefer AS2 over START
            endpointAddress = smpLookupManager.getEndpointTransmissionData(peppolStandardBusinessHeader.getRecipientId(), peppolStandardBusinessHeader.getDocumentTypeIdentifier());
        }

        if (endpointAddress.getBusDoxProtocol() == BusDoxProtocol.AS2 && !sbdhDetected) {
            payload = wrapPayLoadWithSBDH(new ByteArrayInputStream(payload));
        }

        if (endpointAddress.getBusDoxProtocol() == BusDoxProtocol.START && sbdhDetected) {
            throw new IllegalStateException("SBDH should not be used together with the START protocol");
        }

        // Transfers all the properties of this object into the newly created TransmissionRequest
        return new TransmissionRequest(this);

    }

    private byte[] wrapPayLoadWithSBDH(ByteArrayInputStream byteArrayInputStream) {
        SbdhWrapper sbdhWrapper = new SbdhWrapper();
        byte[] result = sbdhWrapper.wrap(byteArrayInputStream);

        return result;
    }

}
