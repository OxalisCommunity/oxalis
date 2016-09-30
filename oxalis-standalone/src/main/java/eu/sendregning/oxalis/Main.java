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

package eu.sendregning.oxalis;

import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.outbound.OxalisOutboundModule;
import eu.peppol.outbound.transmission.TransmissionRequest;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.transmission.TransmissionResponse;
import eu.peppol.outbound.transmission.Transmitter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author ravnholt
 * @author Steinar O. Cook
 * @author Nigel Parker
 * @author Thore Johnsen
 */
public class Main {

    private static OptionSpec<File> xmlDocument;
    private static OptionSpec<String> sender;
    private static OptionSpec<String> recipient;
    private static OptionSpec<String> destinationUrl;
    private static OptionSpec<String> transmissionMethod;   // The protocol START or AS2
    private static OptionSpec<Boolean> trace;
    private static OptionSpec<String> destinationSystemId;  // The AS2 destination system identifier
    private static OptionSpec<String> docType;              // The PEPPOL document type (very long string)
    private static OptionSpec<String> profileType;          // The PEPPOL document profile

    public static void main(String[] args) throws Exception {

        OptionParser optionParser = getOptionParser();

        if (args.length == 0) {
            System.out.println("");
            optionParser.printHelpOn(System.out);
            System.out.println("");
            return;
        }

        OptionSet optionSet;

        try {
            optionSet = optionParser.parse(args);
        } catch (Exception e) {
            printErrorMessage(e.getMessage());
            return;
        }

        File xmlInvoice = xmlDocument.value(optionSet);

        if (!xmlInvoice.exists()) {
            printErrorMessage("XML document " + xmlInvoice + " does not exist");
            return;
        }

        String recipientId = recipient.value(optionSet);
        String senderId = sender.value(optionSet);


        try {

            System.out.println("");
            System.out.println("");

            // bootstraps the Oxalis outbound module
            OxalisOutboundModule oxalisOutboundModule = new OxalisOutboundModule();

            // creates a transmission request builder and enable tracing
            TransmissionRequestBuilder requestBuilder = oxalisOutboundModule.getTransmissionRequestBuilder();
            requestBuilder.trace(trace.value(optionSet));
            System.out.println("Trace mode of RequestBuilder: " + requestBuilder.isTraceEnabled());

            // add receiver participant
            if (recipientId != null) {
                requestBuilder.receiver(new ParticipantId(recipientId));
            }

            // add sender participant
            if (senderId != null) {
                requestBuilder.sender((new ParticipantId(senderId)));
            }

            if (docType != null && docType.value(optionSet) != null) {
                requestBuilder.documentType(PeppolDocumentTypeId.valueOf(docType.value(optionSet)));
            }

            if (profileType != null && profileType.value(optionSet) != null) {
                requestBuilder.processType(PeppolProcessTypeId.valueOf(profileType.value(optionSet)));
            }

            // Supplies the payload
            requestBuilder.payLoad(new FileInputStream(xmlInvoice));

            // Overrides the destination URL if so requested
            if (optionSet.has(destinationUrl)) {
                String destinationString = destinationUrl.value(optionSet);
                URL destination;

                try {
                    destination = new URL(destinationString);
                } catch (MalformedURLException e) {
                    printErrorMessage("Invalid destination URL " + destinationString);
                    return;
                }

                // Fetches the transmission method, which was overridden on the command line
                BusDoxProtocol busDoxProtocol = BusDoxProtocol.instanceFrom(transmissionMethod.value(optionSet));

                if (busDoxProtocol == BusDoxProtocol.AS2) {
                    String accessPointSystemIdentifier = destinationSystemId.value(optionSet);
                    if (accessPointSystemIdentifier == null) {
                        throw new IllegalStateException("Must specify AS2 system identifier if using AS2 protocol");
                    }
                    requestBuilder.overrideAs2Endpoint(destination, accessPointSystemIdentifier);
                } else {
                    throw new IllegalStateException("Unknown busDoxProtocol : " + busDoxProtocol);
                }
            }

            // Specifying the details completed, creates the transmission request
            TransmissionRequest transmissionRequest = requestBuilder.build();

            // Fetches a transmitter ...
            Transmitter transmitter = oxalisOutboundModule.getTransmitter();

            // ... and performs the transmission
            TransmissionResponse transmissionResponse = transmitter.transmit(transmissionRequest);

            // Write the transmission id and where the message was delivered
            System.out.printf("Message using messageId %s sent to %s using %s was assigned transmissionId %s\n",
                    transmissionResponse.getStandardBusinessHeader().getMessageId().stringValue(),
                    transmissionResponse.getURL().toExternalForm(),
                    transmissionResponse.getProtocol().toString(),
                    transmissionResponse.getTransmissionId()
                );

            String evidenceFileName = transmissionResponse.getTransmissionId().toString() + "-evidence.dat";
            IOUtils.copy(new ByteArrayInputStream(transmissionResponse.getEvidenceBytes()), new FileOutputStream(evidenceFileName));
            System.out.printf("Wrote transmission receipt to " + evidenceFileName);

        } catch (Exception e) {
            System.out.println("");
            System.out.println("Message failed : " + e.getMessage());
            //e.printStackTrace();
            System.out.println("");
        }
    }

    private static void printErrorMessage(String message) {
        System.out.println("");
        System.out.println("*** " + message);
        System.out.println("");
    }

    static OptionParser getOptionParser() {
        OptionParser optionParser = new OptionParser();
        docType = optionParser.accepts("d", "Document type").withRequiredArg();
        profileType = optionParser.accepts("p", "Profile type").withRequiredArg();
        xmlDocument = optionParser.accepts("f", "XML document file to be sent").withRequiredArg().ofType(File.class).required();
        sender = optionParser.accepts("s", "sender [e.g. 9908:976098897]").withRequiredArg();
        recipient = optionParser.accepts("r", "recipient [e.g. 9908:976098897]").withRequiredArg();
        destinationUrl = optionParser.accepts("u", "destination URL").withRequiredArg();
        transmissionMethod = optionParser.accepts("m", "method of transmission: start or as2").requiredIf("u").withRequiredArg();
        destinationSystemId = optionParser.accepts("id","AS2 System identifier, obtained from CN attribute of X.509 certificate").withRequiredArg();
        trace = optionParser.accepts("t", "Trace/log/dump on transport level").withOptionalArg().ofType(Boolean.class).defaultsTo(false);
        return optionParser;
    }

    @SuppressWarnings("unused")
    private static String enterPassword() {
        System.out.print("Keystore password: ");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String password = null;
        try {
            password = bufferedReader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                bufferedReader.close();
            } catch (Exception e) {
                /* do nothing */
            }
        }
        return password;
    }

}
