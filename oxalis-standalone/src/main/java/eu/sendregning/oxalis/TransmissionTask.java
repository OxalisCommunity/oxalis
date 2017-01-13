/*
 * Copyright (c) 2010 - 2017 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.sendregning.oxalis;

import eu.peppol.BusDoxProtocol;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.Transmitter;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author steinar
 *         Date: 07.01.2017
 *         Time: 22.43
 */
public class TransmissionTask implements Callable<TransmissionResult> {

    public static final Logger log = LoggerFactory.getLogger(TransmissionTask.class);

    private final TransmissionParameters params;
    private final File xmlPayloadFile;

    public TransmissionTask(TransmissionParameters params, File xmlPayloadFile) {
        this.params = params;
        this.xmlPayloadFile = xmlPayloadFile;
    }

    @Override
    public TransmissionResult call() throws Exception {

        TransmissionRequest transmissionRequest = createTransmissionRequest(params, xmlPayloadFile);

        // Performs the transmission
        Transmitter transmitter = params.getOxalisOutboundComponent().getSimpleTransmitter();

        long start = System.nanoTime();
        TransmissionResponse transmissionResponse = performTransmission(params.getEvidencePath(), transmitter, transmissionRequest);
        long elapsed = System.nanoTime() - start;
        long duration = TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS);

        return new TransmissionResult(duration, transmissionResponse);
    }

    protected static TransmissionRequest createTransmissionRequest(TransmissionParameters params, File xmlPayloadFile) {
        try {

            // creates a transmission request builder and enables trace
            TransmissionRequestBuilder requestBuilder = params.getOxalisOutboundComponent().getTransmissionRequestBuilder();
            requestBuilder.trace(params.getTrace());
            log.info("Trace mode of RequestBuilder: " + requestBuilder.isTraceEnabled());


            // add receiver participant
            if (params.getReceiver().isPresent()) {
                requestBuilder.receiver(params.getReceiver().get());
            }

            // add sender participant
            if (params.getSender().isPresent()) {
                requestBuilder.sender(params.getSender().get());
            }

            if (params.getDocType().isPresent()) {
                requestBuilder.documentType(params.getDocType().get());
            }

            if (params.getProcessTypeId().isPresent()) {
                requestBuilder.processType(params.getProcessTypeId().get());
            }

            // Supplies the payload
            requestBuilder.payLoad(new FileInputStream(xmlPayloadFile));

            // Overrides the destination URL if so requested
            if (params.getDestinationUrl().isPresent()) {
                URL destination = params.getDestinationUrl().get();

                if (!params.getBusDoxProtocol().isPresent()) {
                    throw new IllegalArgumentException("BusDox protocol must be specified if URL is overridden");
                }
                if (!params.getDestinationSystemId().isPresent()) {
                    throw new IllegalArgumentException("Must specify the System id of the destination AP if overriding the end point URL");
                }
                // Fetches the transmission method, which was overridden on the command line
                if (params.getBusDoxProtocol().get() == BusDoxProtocol.AS2) {
                    String accessPointSystemIdentifier = params.getDestinationSystemId().get();
                    if (accessPointSystemIdentifier == null) {
                        throw new IllegalStateException("Must specify AS2 system identifier of receiver AP when using AS2 protocol");
                    }
                    requestBuilder.overrideAs2Endpoint(destination, accessPointSystemIdentifier);
                } else {
                    throw new IllegalStateException("Unknown busDoxProtocol : " + params.getBusDoxProtocol().get());
                }
            }

            // Specifying the details completed, creates the transmission request
            TransmissionRequest transmissionRequest = requestBuilder.build();
            return transmissionRequest;


        } catch (Exception e) {
            System.out.println("");
            System.out.println("Message failed : " + e.getMessage());
            //e.printStackTrace();
            System.out.println("");
            return null;
        }
    }

    protected static TransmissionResponse performTransmission(File evidencePath, Transmitter transmitter, TransmissionRequest transmissionRequest) throws OxalisTransmissionException, IOException {

        // ... and performs the transmission
        long start = System.nanoTime();
        TransmissionResponse transmissionResponse = transmitter.transmit(transmissionRequest);
        long elapsed = System.nanoTime() - start;

        long durartionInMs = TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS);
        // Write the transmission id and where the message was delivered
        log.debug("Message using messageId %s sent to %s using %s was assigned transmissionId %s taking %dms\n",
                transmissionResponse.getStandardBusinessHeader().getInstanceId(),
                transmissionResponse.getURL().toExternalForm(),
                transmissionResponse.getProtocol().getValue(),
                transmissionResponse.getMessageId(),
                durartionInMs
        );

        saveEvidence(transmissionResponse, evidencePath);

        return transmissionResponse;
    }

    protected static void saveEvidence(TransmissionResponse transmissionResponse, File evidencePath) throws IOException {

        saveEvidence(transmissionResponse, "-rem-evidence.xml", transmissionResponse::getRemEvidenceBytes, evidencePath);
        saveEvidence(transmissionResponse, "-as2-mdn.txt", transmissionResponse::getNativeEvidenceBytes, evidencePath);
    }


    static void saveEvidence(TransmissionResponse transmissionResponse, String suffix, Supplier<byte[]> supplier, File evidencePath) throws IOException {
        String fileName = transmissionResponse.getMessageId().toString() + suffix;
        File evidenceFile = new File(evidencePath, fileName);

        IOUtils.copy(new ByteArrayInputStream(supplier.get()), new FileOutputStream(evidenceFile));
        System.out.println("Evidence written to " + evidenceFile);
    }
}
