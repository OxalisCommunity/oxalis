package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.sbdh.SbdhParser;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.util.Util;

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


    @Inject
    SbdhParser sbdhParser;

    @Inject
    SmpLookupManager smpLookupManager;


    byte[] payload;
    private PeppolStandardBusinessHeader peppolStandardBusinessHeader;
    private SmpLookupManager.PeppolEndpointData endpointAddress;

    public TransmissionRequestBuilder contentWithStandardBusinessHeader(InputStream inputStream) {

        try {
            payload = Util.intoBuffer(inputStream);
            peppolStandardBusinessHeader = sbdhParser.parse(new ByteArrayInputStream(payload));

            endpointAddress = smpLookupManager.getEndpointData(peppolStandardBusinessHeader.getRecipientId(), peppolStandardBusinessHeader.getDocumentTypeIdentifier());

        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from inputstream");
        }

        return this;
    }

    public PeppolStandardBusinessHeader getPeppolStandardBusinessHeader() {
        return peppolStandardBusinessHeader;
    }

    public TransmissionRequest build() {

        return new TransmissionRequest(this);

    }
}
