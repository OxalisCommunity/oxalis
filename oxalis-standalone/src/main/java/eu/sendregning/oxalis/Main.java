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
import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.outbound.OxalisOutboundComponent;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ravnholt
 * @author Steinar O. Cook
 * @author Nigel Parker
 * @author Thore Johnsen
 */
public class Main {

    public static final Logger log = LoggerFactory.getLogger(Main.class);

    private static OptionSpec<String> fileSpec;
    private static OptionSpec<String> sender;
    private static OptionSpec<String> recipient;
    private static OptionSpec<String> destinationUrl;
    private static OptionSpec<String> transmissionMethod;   // The protocol START or AS2
    private static OptionSpec<Boolean> trace;
    private static OptionSpec<String> destinationSystemId;  // The AS2 destination system identifier
    private static OptionSpec<String> docType;              // The PEPPOL document type (very long string)
    private static OptionSpec<String> profileType;          // The PEPPOL document profile
    private static OptionSpec<File> evidencePath;  // Path to persistent storage of evidence data
    private static OptionSpec<Integer> threadCount; // Number of paralell threads to use

    public static void main(String[] args) throws Exception {

        OptionParser optionParser = getOptionParser();

        if (args.length == 0) {
            System.out.println("");
            optionParser.printHelpOn(System.out);
            System.out.println("");
            System.out.println("Configure logging: java -Dlogback.configurationFile=/path/to/config.xml -jar <this_jar_file> options");
            return;
        }

        OptionSet optionSet;

        try {
            optionSet = optionParser.parse(args);
        } catch (Exception e) {
            printErrorMessage(e.getMessage());
            return;
        }

        // bootstraps the Oxalis outbound module
        OxalisOutboundComponent oxalisOutboundComponent = new OxalisOutboundComponent();
        TransmissionParameters params = new TransmissionParameters(oxalisOutboundComponent);

        // Verifies the existence of a directory in which transmission evidence is stored.
        File evidencePath = Main.evidencePath.value(optionSet);
        if (evidencePath == null) {
            evidencePath = new File(".");   // Default is current directory
        }
        if (!evidencePath.exists() || !evidencePath.isDirectory()) {
            printErrorMessage(evidencePath + " does not exist or is not a directory");
        }
        params.setEvidencePath(evidencePath);

        // --- Recipient
        String recipientId = recipient.value(optionSet);
        if (recipientId != null) {
            params.setReceiver(Optional.of(new ParticipantId(recipientId)));
        }

        // --- Sender
        String senderId = sender.value(optionSet);
        if (senderId != null) {
            params.setSender(Optional.of(new ParticipantId(senderId)));
        }

        // --- Document type
        if (docType != null && docType.value(optionSet) != null) {
            String value = docType.value(optionSet);
            params.setDocType(Optional.of(PeppolDocumentTypeId.valueOf(value)));
        }

        // --- Process type
        if (profileType != null && profileType.value(optionSet) != null) {
            String value = profileType.value(optionSet);
            params.setProcessTypeId(Optional.of(PeppolProcessTypeId.valueOf(value)));
        }

        // --- Destination URL, protocl and system identifier
        if (optionSet.has(destinationUrl)) {
            String destinationString = destinationUrl.value(optionSet);
            URL destination;

            try {
                destination = new URL(destinationString);
                params.setDestinationUrl(Optional.of(destination));
            } catch (MalformedURLException e) {
                printErrorMessage("Invalid destination URL " + destinationString);
                return;
            }

            // Fetches the transmission method, which was overridden on the command line
            BusDoxProtocol busDoxProtocol = BusDoxProtocol.instanceFrom(transmissionMethod.value(optionSet));
            params.setBusDoxProtocol(Optional.of(busDoxProtocol));

            if (busDoxProtocol == BusDoxProtocol.AS2) {
                String accessPointSystemIdentifier = destinationSystemId.value(optionSet);
                if (accessPointSystemIdentifier == null) {
                    throw new IllegalStateException("Must specify AS2 system identifier of receiver AP when using AS2 protocol");
                }
                params.setDestinationSystemId(Optional.of(accessPointSystemIdentifier));
            } else {
                throw new IllegalStateException("Unknown busDoxProtocol : " + busDoxProtocol);
            }
        }

        // Retrieves the name of the file to be transmitted
        String payloadFileSpec = fileSpec.value(optionSet);

        List<File> files = locateFiles(payloadFileSpec);


        System.out.println("");
        System.out.println("");


        Integer maxThreads = optionSet.valueOf(threadCount);
        log.info("Using " + maxThreads + " threads");

        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);

        long start = System.nanoTime();
        List<Future<TransmissionResult>> result = new ArrayList<>();
        for (File file : files) {

            if (!file.isFile() || !file.canRead()) {
                log.error("File " + file + " is not a file or can not be read, skipping...");
                continue;
            }

            TransmissionTask transmissionTask = new TransmissionTask(params, file);

            Future<TransmissionResult> submit = executorService.submit(transmissionTask);
            result.add(submit);
        }

        // Waits for the results
        List<TransmissionResult> results = new ArrayList<>();
        for (Future<TransmissionResult> transmissionResultFuture : result) {
            TransmissionResult transmissionResult = transmissionResultFuture.get();
            results.add(transmissionResult);
        }

        long elapsed = System.nanoTime() - start;

        executorService.shutdownNow();

        for (TransmissionResult transmissionResult : results) {
            MessageId messageId = transmissionResult.getTransmissionResponse().getMessageId();
            System.out.println(messageId + " transmission took " + transmissionResult.getDuration() + "ms");
        }


        OptionalDouble average = results.stream().mapToLong(r -> r.getDuration()).average();

        if (average.isPresent()) {
            System.out.println("Average transmission time was " + average.getAsDouble() + "ms");
        }
        System.out.println("Total time spent: " + TimeUnit.SECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
        System.out.println("Attempted to send " + results.size() + " files");
        System.out.println("Transmission speed " + results.size() / TimeUnit.SECONDS.convert(elapsed, TimeUnit.NANOSECONDS));

        Thread.sleep(2000);
    }

    static List<File> locateFiles(String payloadFileSpec) {

        List<File> files = new ArrayList<>();
        if (payloadFileSpec == "-") {

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            List<File> collect = reader.lines()
                    .map(s -> new File(s))
                    .collect(Collectors.toList());
            files = collect;
            // Reads list of files from stdin
        } else {

            File fileSpec = new File(payloadFileSpec);
            if (fileSpec.isDirectory()) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(payloadFileSpec), "*.{XML,xml}")) {
                    for (Path path : stream) {
                        files.add(path.toFile());
                    }

                } catch (IOException e) {
                    throw new IllegalStateException("Unable to list files " + payloadFileSpec + "; " + e.getMessage(), e);
                }
            } else if (fileSpec.isFile()) {
                files.add(fileSpec);
            }
        }

        return files;
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
        fileSpec = optionParser.accepts("f", "File(s) to be transmitted").withRequiredArg().ofType(String.class).required();
        sender = optionParser.accepts("s", "sender [e.g. 9908:976098897]").withRequiredArg();
        recipient = optionParser.accepts("r", "recipient [e.g. 9908:976098897]").withRequiredArg();
        destinationUrl = optionParser.accepts("u", "destination URL").withRequiredArg();
        transmissionMethod = optionParser.accepts("m", "method of transmission: start or as2").requiredIf("u").withRequiredArg();
        destinationSystemId = optionParser.accepts("id", "AS2 System identifier of receiver's AP, obtained from CN attribute of X.509 certificate").withRequiredArg();
        trace = optionParser.accepts("t", "Trace/log/dump on transport level").withOptionalArg().ofType(Boolean.class).defaultsTo(false);
        evidencePath = optionParser.accepts("e", "Evidence storage dir").withRequiredArg().ofType(File.class);
        threadCount = optionParser.accepts("x", "Number of threads to use ").withRequiredArg().ofType(Integer.class).defaultsTo(10);

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
