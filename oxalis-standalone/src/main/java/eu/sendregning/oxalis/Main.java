package eu.sendregning.oxalis;

import eu.peppol.outbound.api.DocumentSender;
import eu.peppol.outbound.api.DocumentSenderBuilder;
import eu.peppol.start.identifier.DocumentTypeIdentifierAcronym;
import eu.peppol.start.identifier.DocumentTypeIdentifier;
import eu.peppol.start.identifier.MessageId;
import eu.peppol.start.identifier.ProcessId;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.BufferedReader;
import java.io.File;
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
    private static OptionSpec<DocumentTypeIdentifier> documentType;
    private static OptionSpec<ProcessId> processType;
    private static OptionSpec<String> sender;
    private static OptionSpec<String> recipient;
    private static OptionSpec<File> keystore;
    private static OptionSpec<String> keystorePassword;
    private static OptionSpec<String> destinationUrl;
    private static OptionSpec<String> channel;

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
        String channelId = optionSet.has(channel) ? channel.value(optionSet) : null;
        String password;

        if (optionSet.has(keystorePassword)) {
            password = keystorePassword.value(optionSet);
        } else {
            password = enterPassword();
        }

        DocumentTypeIdentifier documentId = optionSet.has(documentType) ? documentType.value(optionSet) : DocumentTypeIdentifierAcronym.INVOICE.getDocumentTypeIdentifier();
        ProcessId processId = optionSet.has(processType) ? processType.value(optionSet) : getDefaultProcess(documentId);
        DocumentSender documentSender;

        try {
            documentSender = new DocumentSenderBuilder()
                    .setDocumentTypeIdentifier(documentId)
                    .setProcessId(processId)
                    .setKeystoreFile(keystore.value(optionSet))
                    .setKeystorePassword(password)
                    .build();
        } catch (Exception e) {
            printErrorMessage(e.getMessage());
            return;
        }

        try {
            System.out.println("");
            System.out.println("");

            // Holds the messageId assigned upon successful transmission
            MessageId messageId = null;

            if (optionSet.has(destinationUrl)) {
                String destinationString = destinationUrl.value(optionSet);
                URL destination;

                try {
                    destination = new URL(destinationString);
                } catch (MalformedURLException e) {
                    printErrorMessage("Invalid destination URL " + destinationString);
                    return;
                }

                messageId = documentSender.sendInvoice(xmlInvoice, senderId, recipientId, destination, channelId);
            } else {
                messageId = documentSender.sendInvoice(xmlInvoice, senderId, recipientId, channelId);
            }

            System.out.println("Message sent, assigned message id:" + messageId);

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

    private static ProcessId getDefaultProcess(DocumentTypeIdentifier documentId) {
        // todo: implement more extensible logic to deal with the various legal combinations of document and
        // process types
        return documentId.equals(DocumentTypeIdentifierAcronym.ORDER) ?
                ProcessId.ORDER_ONLY :
                ProcessId.INVOICE_ONLY;
    }

    private static OptionParser getOptionParser() {
        OptionParser optionParser = new OptionParser();
        xmlDocument = optionParser.accepts("f", "XML document file to be sent").withRequiredArg().ofType(File.class).required();
        documentType = optionParser.accepts("d", "Document type").withRequiredArg().ofType(DocumentTypeIdentifier.class);
        processType = optionParser.accepts("p", "Process type").withRequiredArg().ofType(ProcessId.class);
        channel = optionParser.accepts("c", "Channel identification").withRequiredArg();
        sender = optionParser.accepts("s", "sender [e.g. 9908:976098897]").withRequiredArg().required();
        recipient = optionParser.accepts("r", "recipient [e.g. 9908:976098897]").withRequiredArg().required();
        keystore = optionParser.accepts("kf", "keystore file").withRequiredArg().ofType(File.class).required();
        keystorePassword = optionParser.accepts("kp", "keystore password").withRequiredArg();
        destinationUrl = optionParser.accepts("u", "destination URL").withRequiredArg();
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
