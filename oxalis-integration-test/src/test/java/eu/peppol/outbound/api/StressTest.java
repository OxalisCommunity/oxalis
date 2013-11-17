package eu.peppol.outbound.api;

import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeIdAcronym;
import eu.peppol.outbound.util.Log;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.assertNotNull;

/**
 * Performs a stress test of the access point sending the number of messages specified in
 * MESSAGES to the access point.
 *
 * User: nigel
 * Date: Dec 7, 2011
 * Time: 7:47:11 PM
 */
public class StressTest  {

    private static final long MESSAGES = 100;
    private static final int THREADS = 30;
    protected static final String START_SERVICE_END_POINT = "https://localhost:8443/oxalis/accessPointService";


    @Test(groups = {"manual"})
    public void test01() throws Exception {

        final DocumentSender documentSender = new DocumentSenderBuilder().setDocumentTypeIdentifier(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier())
                .setPeppolProcessTypeId(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId())
                .build();

        final List<Callable<Integer>> partitions = new ArrayList<Callable<Integer>>();

        long start = System.currentTimeMillis();

        for (int i = 1; i <= MESSAGES; i++) {

            partitions.add(new Callable<Integer>() {
                public Integer call() throws Exception {
                    sendDocument(documentSender);
                    getMemoryUsage();
                    return 1;
                }
            });
        }

        final ExecutorService executorPool = Executors.newFixedThreadPool(THREADS);
        final List<Future<Integer>> values = executorPool.invokeAll(partitions, 1000, TimeUnit.SECONDS);
        int sum = 0;

        for (Future<Integer> result : values) {
            sum += result.get();
        }

        executorPool.shutdown();
        long millis = System.currentTimeMillis() - start;
        long seconds = millis / 1000;
        long rate = sum / seconds;
        System.out.println("");
        System.out.println("");
        System.out.println("%%% " + sum + " messages in " + seconds + " seconds, " + rate + " messages per second");
        System.out.println("");
    }

    private void sendDocument(DocumentSender documentSender) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("logback-test.xml");
        assertNotNull(inputStream, "Unable to locate file logback-test.xml in class path");

        documentSender.sendInvoice(
                inputStream,
                "9908:976098897",
                "9908:976098897",
                new URL(START_SERVICE_END_POINT),
                ""
        );

        inputStream.close();
    }

    private static final long MEMORY_THRESHOLD = 10;
    private static long lastUsage = 0;

    /**
     * returns a String describing current memory utilization. In addition unusually large
     * changes in memory usage will be logged.
     */
    public static String getMemoryUsage() {

        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long usedMemory = totalMemory - freeMemory;
        final long mega = 1048576;
        long usedInMegabytes = usedMemory / mega;
        long totalInMegabytes = totalMemory / mega;
        String memoryStatus = usedInMegabytes + "M / " + totalInMegabytes + "M / " + (runtime.maxMemory() / mega) + "M";

        if (usedInMegabytes <= lastUsage - MEMORY_THRESHOLD || usedInMegabytes >= lastUsage + MEMORY_THRESHOLD) {
            Log.info("Memory usage: " + memoryStatus);
            lastUsage = usedInMegabytes;
        }

        return memoryStatus;
    }

}
