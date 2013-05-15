package eu.peppol.statistics;

import eu.peppol.security.OxalisCipher;
import eu.peppol.security.StatisticsKeyTool;
import eu.peppol.start.identifier.AccessPointIdentifier;
import eu.peppol.statistics.repository.DownloadRepository;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Downloads statistics for each access point, for which the required data like for instance the AP identity and
 * URL is provided by a list of {@link AccessPointMetaData}.
 * <p/>
 * <p>Downloaded data is saved (persisted) in a {@link DownloadRepository}</p>
 * <p/>
 * <p>Data is downloaded concurrently from all the access points by using Java Futures. A Future is a task for which
 * the results may be collected at some point in the future.</p>
 *
 * @author steinar
 *         Date: 07.03.13
 *         Time: 15:55
 */
public class StatisticsDownloader {

    public static final Logger log = LoggerFactory.getLogger(StatisticsDownloader.class);
    public static final int HTTP_CONNECTION_TIMEOUT = 30; // Seconds

    private final DownloadRepository downloadRepository;


    public StatisticsDownloader(DownloadRepository downloadRepository) {

        this.downloadRepository = downloadRepository;
    }


    /**
     * Downloads the contents from each URL in the supplied list of AccessPointMetaData
     *
     * @param accessPointMetaDataList list of AccessPointMetaData
     */
    public List<DownloadResult> download(List<AccessPointMetaData> accessPointMetaDataList) {


        // Creates the Apache Http client with pooling connection properties
        HttpClient httpClient = createHttpClient();


        // Holds our pool of workers, which will execute the download tasks (futures)
        ExecutorService executor = Executors.newFixedThreadPool(accessPointMetaDataList.size());

        try {
            List<DownloadTask> downloadTasks = createDownloadTasks(accessPointMetaDataList, httpClient);

            List<Future<DownloadResult>> futures = submitTasksforExceution(executor, downloadTasks);

            List<DownloadResult> downloadResults = collectResultsOfTaskExecution(futures);

            return downloadResults;
        } finally {
            // Assuming all our workers have either completed or been interrupted, we close
            // our connection and the execution manager
            httpClient.getConnectionManager().shutdown();
            executor.shutdown();
        }
    }


    private List<DownloadResult> collectResultsOfTaskExecution(List<Future<DownloadResult>> futures) {
        List<DownloadResult> downloadResults = new ArrayList<DownloadResult>();

        // Iterates the results and adds each one into our list of results.
        for (Future<DownloadResult> future : futures) {
            try {
                DownloadResult downloadResult = future.get();
                downloadResults.add(downloadResult);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Error: " + downloadResults + "; " + e, e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Execution failed " + e.getCause(), e.getCause());
            }
        }
        return downloadResults;
    }

    private List<Future<DownloadResult>> submitTasksforExceution(ExecutorService executor, List<DownloadTask> downloadTasks) {
        List<Future<DownloadResult>> futures = null;
        try {
            futures = executor.invokeAll(downloadTasks, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Execution of tasks interrupted " + e, e);
        }
        return futures;
    }


    private List<DownloadTask> createDownloadTasks(List<AccessPointMetaData> accessPointMetaDataList,  HttpClient httpClient) {
        log.debug("Creating tasks ...");

        // Private key is needed for decryption of encrypted contents from each URL in each DownloadTask
        PrivateKey privateKey = new StatisticsKeyTool().loadPrivateKeyFromOxalisHome();

        List<DownloadTask> downloadTasks = new ArrayList<DownloadTask>();
        for (AccessPointMetaData accessPointMetaData : accessPointMetaDataList) {

            DateTime startDateTime = startDateTime(accessPointMetaData);

            URL statisticsUrl = accessPointMetaData.getStatisticsUrl();
            if (statisticsUrl == null) {
            URL url = null;
                try {
                    url = new URL(accessPointMetaData.getAccessPointServiceUrl().toExternalForm() + "/../statistics");

                    // Processes the .. construction
                    statisticsUrl = url.toURI().normalize().toURL();
                } catch (MalformedURLException e) {
                    throw new IllegalStateException("Unable to compose alternative URL for " + accessPointMetaData.getAccessPointServiceUrl().toExternalForm(), e);
                } catch (URISyntaxException e) {
                    throw new IllegalStateException("Unable to normalize " + url.toExternalForm() + "; " + e, e);
                }
            }
            URL downloadUrl = composeDownloadUrl(statisticsUrl, startDateTime, new DateTime());

            // Creates the download task with all required parameters
            downloadTasks.add(new DownloadTask(downloadRepository, privateKey, accessPointMetaData, httpClient, downloadUrl));
        }
        return downloadTasks;
    }


    URL composeDownloadUrl(URL statisticsUrl, DateTime startDateTime, DateTime endDateTime) {

        // Sets the granularity
        StatisticsGranularity statisticsGranularity = StatisticsGranularity.HOUR;

        URI resultUri = null;
        try {
            URI uri = statisticsUrl.toURI();

            String query = null;
            DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateHour();

            String startDateTimeAsString = dateTimeFormatter.print(startDateTime);
            String endDateTimeAsString = dateTimeFormatter.print(endDateTime);
            query = String.format("start=%s&end=%s&granularity=%s", startDateTimeAsString, endDateTimeAsString, statisticsGranularity.getAbbreviation());

            URI result = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), query, null);

            return result.toURL();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URL to URI conversion for " + statisticsUrl + "; " +e,e);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to convert URI " + resultUri + " into a URL." + e, e);
        }
    }


    private DateTime startDateTime(AccessPointMetaData accessPointMetaData) {
        // Sets the start date to date of last successful download
        DateTime startDateTime = downloadRepository.fetchLastTimeStamp(accessPointMetaData.getAccessPointIdentifier());
        if (startDateTime == null) {
            startDateTime = new DateTime("2013-01-01T00:00");
        }

        return  startDateTime;
    }


    HttpClient createHttpClient() {
        // Creates a scheme registry in which all SSL peer certificates are accepted
        SchemeRegistry noSSLSecuritySchemeRegistry = createNoSSLSecuritySchemeRegistry();
        PoolingClientConnectionManager poolingClientConnectionManager = new PoolingClientConnectionManager(noSSLSecuritySchemeRegistry);

        poolingClientConnectionManager.setMaxTotal(100);

        HttpClient httpClient = new DefaultHttpClient(poolingClientConnectionManager);
        HttpParams httpParams = httpClient.getParams();
        // Timeout after 5 seconds
        HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_CONNECTION_TIMEOUT * 1000);
        // Low level socket operations, should timeout after three seconds.
        HttpConnectionParams.setSoTimeout(httpParams, 3 * 1000);
        return httpClient;
    }


    SchemeRegistry createNoSSLSecuritySchemeRegistry() {
        SSLSocketFactory sslSocketFactory = null;
        TrustStrategy trustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                return true;
            }
        };

        X509HostnameVerifier allowAllHostNameVerifier = new X509HostnameVerifier() {

            @Override
            public void verify(String host, SSLSocket sslSocket) throws IOException {
            }

            @Override
            public void verify(String host, X509Certificate x509Certificate) throws SSLException {
            }

            @Override
            public void verify(String host, String[] strings, String[] strings2) throws SSLException {
            }

            @Override
            public boolean verify(String host, SSLSession sslSession) {
                return true;
            }
        };

        try {
            sslSocketFactory = new SSLSocketFactory(trustStrategy, allowAllHostNameVerifier);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create a SSL SocketFactory " + e, e);
        }

        Scheme scheme = new Scheme("https", 443, sslSocketFactory);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(scheme);

        schemeRegistry.register(new Scheme("http", 8080, PlainSocketFactory.getSocketFactory()));

        return schemeRegistry;
    }

}
