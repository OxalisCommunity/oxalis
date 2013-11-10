package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.DocumentSniffer;
import eu.peppol.document.NoSbdhParser;
import eu.peppol.document.SbdhParser;
import eu.peppol.document.SbdhWrapper;import eu.peppol.smp.SmpLookupManager;
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

    @Inject
    SbdhParser sbdhParser;

    @Inject
    NoSbdhParser noSbdhParser;

    @Inject
    SmpLookupManager smpLookupManager;


    byte[] payload;
    private PeppolStandardBusinessHeader peppolStandardBusinessHeader;
    private SmpLookupManager.PeppolEndpointData endpointAddress;

    private boolean sbdhDetected;

    @Inject
    public TransmissionRequestBuilder(SbdhParser sbdhParser, NoSbdhParser noSbdhParser) {
        this.sbdhParser = sbdhParser;
        this.noSbdhParser = noSbdhParser;
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
     * Override the endpoint URL and the transmission protocol. You had better know what you are doing :-)
     *
     * @param url
     * @param busDoxProtocol
     * @return
     */
    public TransmissionRequestBuilder endPoint(URL url, BusDoxProtocol busDoxProtocol) {
        endpointAddress = new SmpLookupManager.PeppolEndpointData(url, busDoxProtocol);
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
            endpointAddress = smpLookupManager.getEndpointData(peppolStandardBusinessHeader.getRecipientId(), peppolStandardBusinessHeader.getDocumentTypeIdentifier());
        }

        if (endpointAddress.getBusDoxProtocol().equals(BusDoxProtocol.AS2) && !sbdhDetected) {
            payload = wrapPayLoadWithSBDH(new ByteArrayInputStream(payload), peppolStandardBusinessHeader);

        }
        // Transfers all the properties of this object into the newly created TransmissionRequest
        return new TransmissionRequest(this);

    }

    private byte[] wrapPayLoadWithSBDH(ByteArrayInputStream byteArrayInputStream, PeppolStandardBusinessHeader peppolStandardBusinessHeader) {
        SbdhWrapper sbdhWrapper = new SbdhWrapper();
        byte[] result = sbdhWrapper.wrap(byteArrayInputStream, peppolStandardBusinessHeader);

        return result;
    }

}
