package eu.sendregning.oxalis;

import com.sun.xml.ws.transport.http.client.HttpTransportPipe;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.*;
import eu.peppol.outbound.OxalisOutboundModule;
import eu.peppol.outbound.transmission.TransmissionResponse;
import eu.peppol.outbound.transmission.TransmissionRequest;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.transmission.Transmitter;
import eu.peppol.smp.SmpLookupManagerImpl;
import eu.peppol.smp.SmpSignedServiceMetaDataException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author ravnholt
 * @author Steinar O. Cook
 * @author Nigel Parker
 */
public class Main {

    private static OptionSpec<File> xmlDocument;
    private static OptionSpec<String> sender;
    private static OptionSpec<String> recipient;
    private static OptionSpec<String> destinationUrl;
    private static OptionSpec<String> transmissionMethod;   // The protocol START or AS2
    private static OptionSpec<Boolean> trace;


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


        // Enable SOAP logging on the client side if -t was specified on the command line
        if (optionSet.has("t")) {
            HttpTransportPipe.dump = true;
        }

        try {
            System.out.println("");
            System.out.println("");

            {
                // Bootstraps the Oxalis outbound module
                OxalisOutboundModule oxalisOutboundModule = new OxalisOutboundModule();

                // Creates a transmission request builder
                TransmissionRequestBuilder requestBuilder = oxalisOutboundModule.getTransmissionRequestBuilder();

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
                    // ... and gives it to the transmission request builder
                    requestBuilder.endPoint(destination, busDoxProtocol);
                }

                // Specifying the details completed, creates the transmission request
                TransmissionRequest transmissionRequest = requestBuilder.build();

                // Fetches a transmitter
                Transmitter transmitter = oxalisOutboundModule.getTransmitter();
                // ....  and performs the transmission
                TransmissionResponse transmissionResponse = transmitter.transmit(transmissionRequest);

                System.out.println("Message sent, assigned message id:" + transmissionResponse.getTransmissionId());
            }
        } catch (Exception e) {
            System.out.println("");
            e.printStackTrace();
            System.out.println("");
        }
    }

    private static void printErrorMessage(String message) {
        System.out.println("");
        System.out.println("*** " + message);
        System.out.println("");
    }

    private static PeppolProcessTypeId getDefaultProcess(ParticipantId participantId, PeppolDocumentTypeId documentId) throws SmpSignedServiceMetaDataException {
        return new SmpLookupManagerImpl().getProcessIdentifierForDocumentType(participantId, documentId);
    }

    static OptionParser getOptionParser() {
        OptionParser optionParser = new OptionParser();
        xmlDocument = optionParser.accepts("file", "XML document file to be sent").withRequiredArg().ofType(File.class).required();
        sender = optionParser.accepts("s", "sender [e.g. 9908:976098897]").withRequiredArg().required();
        recipient = optionParser.accepts("r", "recipient [e.g. 9908:976098897]").withRequiredArg().required();
        destinationUrl = optionParser.accepts("u", "destination URL").withRequiredArg();
        transmissionMethod = optionParser.accepts("m", "method of transmission: start or as2").requiredIf("u").withRequiredArg();
        optionParser.accepts("t", "Trace/log/dump SOAP on transport level");

        return optionParser;
    }

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
            }
        }

        return password;
    }
}
