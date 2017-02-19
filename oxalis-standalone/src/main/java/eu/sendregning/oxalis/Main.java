/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.sendregning.oxalis;

import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import no.difi.certvalidator.Validator;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author Steinar O. Cook
 * @author Nigel Parker
 * @author Thore Johnsen
 * @author erlend
 */
public class Main {

    public static final Logger log = LoggerFactory.getLogger(Main.class);

    private static OptionSpec<String> fileSpec;

    private static OptionSpec<String> sender;

    private static OptionSpec<String> recipient;

    private static OptionSpec<String> docType;              // The PEPPOL document type (very long string)

    private static OptionSpec<String> profileType;          // The PEPPOL document profile

    private static OptionSpec<File> evidencePath;  // Path to persistent storage of evidence data

    private static OptionSpec<Integer> threadCount; // Number of paralell threads to use

    private static OptionSpec<Boolean> useRequestFactory;

    private static OptionSpec<Integer> repeatCount;

    private static OptionSpec<String> destinationUrl;

    private static OptionSpec<File> destinationCertificate;

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

        // --- Use Factory
        params.setUseFactory(useRequestFactory.value(optionSet));

        // --- Recipient
        String recipientId = recipient.value(optionSet);
        if (recipientId != null) {
            params.setReceiver(new ParticipantId(recipientId));
        }

        // --- Sender
        String senderId = sender.value(optionSet);
        if (senderId != null) {
            params.setSender(new ParticipantId(senderId));
        }

        // --- Document type
        if (docType != null && docType.value(optionSet) != null) {
            String value = docType.value(optionSet);
            params.setDocType(PeppolDocumentTypeId.valueOf(value));
        }

        // --- Process type
        if (profileType != null && profileType.value(optionSet) != null) {
            String value = profileType.value(optionSet);
            params.setProcessTypeId(PeppolProcessTypeId.valueOf(value));
        }

        // --- Destination URL, protocl and system identifier
        if (optionSet.has(destinationUrl)) {
            String destinationString = destinationUrl.value(optionSet);

            X509Certificate certificate;
            try (InputStream inputStream = new FileInputStream(destinationCertificate.value(optionSet))) {
                certificate = Validator.getCertificate(inputStream);
            }

            params.setEndpoint(Endpoint.of(TransportProfile.AS2_1_0, URI.create(destinationString), certificate));
        }

        // Retrieves the name of the file to be transmitted
        String payloadFileSpec = fileSpec.value(optionSet);

        List<File> files = locateFiles(payloadFileSpec);


        System.out.println("");
        System.out.println("");


        Integer maxThreads = optionSet.valueOf(threadCount);
        log.info("Using " + maxThreads + " threads");

        int repeats = optionSet.valueOf(repeatCount);

        ExecutorService exec = Executors.newFixedThreadPool(maxThreads);
        ExecutorCompletionService<TransmissionResult> ecs = new ExecutorCompletionService<TransmissionResult>(exec);

        long start = System.nanoTime();
        int submittedTaskCount = 0;
        for (File file : files) {

            if (!file.isFile() || !file.canRead()) {
                log.error("File " + file + " is not a file or can not be read, skipping...");
                continue;
            }

            for (int i = 0; i < repeats; i++) {
                TransmissionTask transmissionTask = new TransmissionTask(params, file);

                Future<TransmissionResult> submit = ecs.submit(transmissionTask);
                submittedTaskCount++;
            }
        }

        // Wait for the results to be available
        List<TransmissionResult> results = new ArrayList<>();
        int failed = 0;
        for (int i = 0; i < submittedTaskCount; i++) {
            try {
                Future<TransmissionResult> future = ecs.take();
                TransmissionResult transmissionResult = future.get();
                results.add(transmissionResult);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            } catch (ExecutionException e) {
                log.error("Execution failed: {}", e.getMessage(), e);
                failed++;
            }
        }

        long elapsed = System.nanoTime() - start;

        exec.shutdownNow(); // Shuts down the executor service

        for (TransmissionResult transmissionResult : results) {
            MessageId messageId = transmissionResult.getTransmissionResponse().getMessageId();
            System.out.println(messageId + " transmission took " + transmissionResult.getDuration() + "ms");
        }


        OptionalDouble average = results.stream().mapToLong(r -> r.getDuration()).average();

        if (average.isPresent()) {
            System.out.println("Average transmission time was " + average.getAsDouble() + "ms");
        }
        long elapsedInMs = TimeUnit.SECONDS.convert(elapsed, TimeUnit.NANOSECONDS);
        System.out.println("Total time spent: " + elapsedInMs);
        System.out.println("Attempted to send " + results.size() + " files");
        System.out.println("Failed transmissions: " + failed);
        if (results.size() > 0 && elapsedInMs > 0) {
            System.out.println("Transmission speed " + results.size() / elapsedInMs);
        }

        Thread.sleep(2000);
    }

    /**
     * Locates the files to load and transmit.
     *
     * @param payloadFileSpec specifies the name of the file or the directory holding files to be transmitted.
     * @return list of eligible files.
     */
    static List<File> locateFiles(String payloadFileSpec) {

        List<File> files = new ArrayList<>();
        if ("-".equals(payloadFileSpec)) {

            // Reads list of files from stdin
            try (Reader reader = new InputStreamReader(System.in);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {
                files = bufferedReader.lines()
                        .map(File::new)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
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

        fileSpec = optionParser.accepts("f", "File(s) to be transmitted")
                .withRequiredArg().ofType(String.class).required();

        docType = optionParser.accepts("d", "Document type").withRequiredArg();
        profileType = optionParser.accepts("p", "Profile type").withRequiredArg();
        sender = optionParser.accepts("s", "sender [e.g. 9908:976098897]").withRequiredArg();
        recipient = optionParser.accepts("r", "recipient [e.g. 9908:976098897]").withRequiredArg();

        evidencePath = optionParser.accepts("e", "Evidence storage dir")
                .withRequiredArg().ofType(File.class);
        threadCount = optionParser.accepts("x", "Number of threads to use ")
                .withRequiredArg().ofType(Integer.class).defaultsTo(10);
        useRequestFactory = optionParser.accepts("factory", "Use TransmissionRequestFactory (no overrides!)")
                .withOptionalArg().ofType(Boolean.class).defaultsTo(false);
        repeatCount = optionParser.accepts("repeat", "Number of repeats to use ")
                .withRequiredArg().ofType(Integer.class).defaultsTo(1);

        destinationUrl = optionParser.accepts("u", "destination URL").withRequiredArg();
        destinationCertificate = optionParser.accepts("cert", "Receiving AP's certificate (when overriding endpoint)")
                .requiredIf("u").withRequiredArg().ofType(File.class);

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
            log.error(e.getMessage(), e);
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
