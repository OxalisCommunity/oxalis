/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

import com.google.common.io.ByteStreams;
import com.google.inject.Key;
import com.google.inject.name.Names;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.commons.certvalidator.Validator;
import network.oxalis.api.model.TransmissionIdentifier;
import network.oxalis.outbound.OxalisOutboundComponent;
import network.oxalis.vefa.peppol.common.model.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

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
 *
 * @author aaron-kumar
 * @since 5.0.0
 */
@Slf4j
public class Main {

    private static OptionSpec<String> fileSpec;

    private static OptionSpec<String> sender;

    private static OptionSpec<String> recipient;

    private static OptionSpec<String> docType;              // The PEPPOL document type (very long string)

    private static OptionSpec<String> profileType;          // The PEPPOL document profile

    private static OptionSpec<File> evidencePath;  // Path to persistent storage of evidence data

    private static OptionSpec<Boolean> useRequestFactory;

    private static OptionSpec<Integer> repeatCount;

    private static OptionSpec<String> destinationUrl;

    private static OptionSpec<File> destinationCertificate;

    private static OptionSpec<String> protocol;

    private static OptionSpec<Integer> maxTransmissions;    // Maximum number of transmissions no matter what

    private static OptionSpec<Boolean> probe;

    private static OptionSpec<String> tag;

    private static OptionSpec<Integer> sleep;

    public static void main(String[] args) throws Exception {

        OptionParser optionParser = getOptionParser();

        if (args.length == 0) {
            System.out.println();
            optionParser.printHelpOn(System.out);
            System.out.println();
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

        // Tag
        params.setTag(tag.value(optionSet));

        // --- Use Factory
        params.setUseFactory(useRequestFactory.value(optionSet));

        // --- Recipient
        String recipientId = recipient.value(optionSet);
        if (recipientId != null) {
            params.setReceiver(ParticipantIdentifier.of(recipientId));
        }

        // --- Sender
        String senderId = sender.value(optionSet);
        if (senderId != null) {
            params.setSender(ParticipantIdentifier.of(senderId));
        }

        // --- Document type
        if (docType != null && docType.value(optionSet) != null) {
            String value = docType.value(optionSet);
            params.setDocType(DocumentTypeIdentifier.of(value));
        }

        // --- Process type
        if (profileType != null && profileType.value(optionSet) != null) {
            String value = profileType.value(optionSet);
            params.setProcessIdentifier(ProcessIdentifier.of(value));
        }

        if (probe.value(optionSet)) {
            CloseableHttpClient httpClient = oxalisOutboundComponent.getInjector()
                    .getInstance(CloseableHttpClient.class);
            try (CloseableHttpResponse response = httpClient.execute(new HttpGet(destinationUrl.value(optionSet)))) {
                ByteStreams.copy(response.getEntity().getContent(), System.out);
            }
        } else {
            // --- Destination URL, protocl and system identifier
            if (optionSet.has(destinationUrl) && !probe.value(optionSet)) {
                String destinationString = destinationUrl.value(optionSet);

                X509Certificate certificate;
                try (InputStream inputStream = new FileInputStream(destinationCertificate.value(optionSet))) {
                    certificate = Validator.getCertificate(inputStream);
                }

                params.setEndpoint(Endpoint.of(
                        TransportProfile.of(protocol.value(optionSet)),
                        URI.create(destinationString),
                        certificate));
            }

            // Retrieves the name of the file to be transmitted
            String payloadFileSpec = fileSpec.value(optionSet);

            List<File> files = locateFiles(payloadFileSpec);


            System.out.println();
            System.out.println();

            int repeats = optionSet.valueOf(repeatCount);
            int maximumTransmissions = optionSet.valueOf(maxTransmissions);

            ExecutorService exec = oxalisOutboundComponent.getInjector()
                    .getInstance(Key.get(ExecutorService.class, Names.named("default")));
            ExecutorCompletionService<TransmissionResult> ecs = new ExecutorCompletionService<>(exec);

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
                    if (submittedTaskCount > maximumTransmissions) {
                        log.info("Stopped submitting tasks at {} " + submittedTaskCount);
                        break;
                    }
                }
                if (submittedTaskCount > maximumTransmissions) {
                    break;
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
                TransmissionIdentifier transmissionIdentifier = transmissionResult.getTransmissionIdentifier();
                System.out.println(String.format("%s transmission took %s ms",
                        transmissionIdentifier, transmissionResult.getDuration()));
            }

            OptionalDouble average = results.stream().mapToLong(TransmissionResult::getDuration).average();

            if (average.isPresent()) {
                System.out.println("Average transmission time was " + average.getAsDouble() + "ms");
            }
            long elapsedInSeconds = TimeUnit.SECONDS.convert(elapsed, TimeUnit.NANOSECONDS);
            System.out.println("Total time spent: " + elapsedInSeconds + "s");
            System.out.println("Attempted to send " + results.size() + " files");
            System.out.println("Failed transmissions: " + failed);
            if (results.size() > 0 && elapsedInSeconds > 0) {
                System.out.println("Transmission speed " + results.size() / elapsedInSeconds + " documents per second");
            }

            // Sleep if set to do so.
            Integer sleepSecs = sleep.value(optionSet);
            if (sleepSecs > 0) {
                Thread.sleep(sleepSecs * 1000);
            }

            System.exit(failed == 0 ? 0 : 1);
        }
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
            if (!fileSpec.exists()) {
                log.warn(String.format("'%s' does not exists.", fileSpec.getAbsolutePath()));
            } else if (fileSpec.isDirectory()) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(payloadFileSpec), "*.{XML,xml}")) {
                    for (Path path : stream) {
                        files.add(path.toFile());
                    }

                } catch (IOException e) {
                    throw new IllegalStateException(
                            String.format("Unable to list files %s; %s", payloadFileSpec, e.getMessage()), e);
                }
            } else if (fileSpec.isFile()) {
                files.add(fileSpec);
            } else {
                log.warn(String.format("'%s' is neither file nor directory.", fileSpec.getAbsoluteFile()));
            }
        }

        return files;
    }


    private static void printErrorMessage(String message) {
        System.out.println();
        System.out.println("*** " + message);
        System.out.println();
    }

    static OptionParser getOptionParser() {
        OptionParser optionParser = new OptionParser();

        fileSpec = optionParser.accepts("f", "File(s) to be transmitted")
                .withRequiredArg().ofType(String.class);

        docType = optionParser.accepts("d", "Document type")
                .withRequiredArg();
        profileType = optionParser.accepts("p", "Profile type")
                .withRequiredArg();
        sender = optionParser.accepts("s", "sender [e.g. 9908:976098897]")
                .withRequiredArg();
        recipient = optionParser.accepts("r", "recipient [e.g. 9908:976098897]")
                .withRequiredArg();

        evidencePath = optionParser.accepts("e", "Evidence storage dir")
                .withRequiredArg().ofType(File.class);
        useRequestFactory = optionParser.accepts("factory", "Use TransmissionRequestFactory (no overrides!)")
                .withOptionalArg().ofType(Boolean.class).defaultsTo(false);
        repeatCount = optionParser.accepts("repeat", "Number of repeats to use ")
                .withRequiredArg().ofType(Integer.class).defaultsTo(1);

        probe = optionParser.accepts("probe", "Perform probing of endpoint.")
                .withRequiredArg().ofType(Boolean.class).defaultsTo(false);

        destinationUrl = optionParser.accepts("u", "destination URL")
                .requiredIf(probe, protocol).withRequiredArg();
        destinationCertificate = optionParser.accepts("cert", "Receiving AP's certificate (when overriding endpoint)")
                .requiredIf(destinationUrl).withRequiredArg().ofType(File.class);

        protocol = optionParser.accepts("protocol", "Protocol to be used")
                .withRequiredArg().ofType(String.class).defaultsTo(TransportProfile.AS2_1_0.getIdentifier());

        maxTransmissions = optionParser.accepts("m", "Max number of transmissions")
                .withRequiredArg().ofType(Integer.class).defaultsTo(Integer.MAX_VALUE);

        tag = optionParser.accepts("tag", "User defined tag")
                .withRequiredArg();

        sleep = optionParser.accepts("sleep", "Sleep standalone for x seconds after transmission.")
                .withRequiredArg().ofType(Integer.class).defaultsTo(0);

        return optionParser;
    }

    @SuppressWarnings("unused")
    private static String enterPassword() {
        System.out.print("Keystore password: ");
        String password = null;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            password = bufferedReader.readLine();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }
        return password;
    }
}
