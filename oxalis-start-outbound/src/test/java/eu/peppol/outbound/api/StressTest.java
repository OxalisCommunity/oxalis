package eu.peppol.outbound.api;

import eu.peppol.outbound.util.Log;
import eu.peppol.outbound.util.TestBase;
import eu.peppol.start.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.start.identifier.PeppolProcessTypeIdAcronym;
import org.testng.Assert;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * User: nigel
 * Date: Dec 7, 2011
 * Time: 7:47:11 PM
 */
//@Test
public class StressTest extends TestBase {

    private static final long MESSAGES = 100;
    private static final int THREADS = 30;
//    public static final String KEYSTORE_FILE = "/usr/local/apache-tomcat-7.0.21/conf/keystore/keystore.jks";
    public static final String KEYSTORE_FILE = "/Users/steinar/appl/apache-tomcat-7.0.22/conf/keystore/keystore.jks";
    protected static final String START_SERVICE_END_POINT = "https://localhost:8443/oxalis/accessPointService";


    public void test01() throws Exception {

        File keystore = new File(KEYSTORE_FILE);
        Assert.assertTrue(keystore.exists() && keystore.canRead(), KEYSTORE_FILE + " not found, check the path :-)");
        final DocumentSender documentSender = new DocumentSenderBuilder().setDocumentTypeIdentifier(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier())
                .setPeppolProcessTypeId(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId())
                .setKeystoreFile(new File(KEYSTORE_FILE))
                .setKeystorePassword("peppol")
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
