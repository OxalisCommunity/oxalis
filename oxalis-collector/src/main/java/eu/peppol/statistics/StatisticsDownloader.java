package eu.peppol.statistics;

import eu.peppol.statistics.repository.DownloadRepository;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author steinar
 *         Date: 07.03.13
 *         Time: 15:55
 */
public class StatisticsDownloader {

    public static final Logger log = LoggerFactory.getLogger(StatisticsDownloader.class);
    private final DownloadRepository downloadRepository;


    public StatisticsDownloader(DownloadRepository downloadRepository) {

        this.downloadRepository = downloadRepository;
    }


    /**
     * Downloads the contents from each URL in the supplied list of AccessPointMetaData
     *
     * @param accessPointMetaDataList list of AccessPointMetaData
     * @param urlRewriter             if supplied, this object will be invoked to rewrite (modify) the URL before starting the download
     */
    public void download(List<AccessPointMetaData> accessPointMetaDataList, URLRewriter urlRewriter) {


        HttpClient httpClient = createHttpClient();

        log.debug("Creating tasks ...");

        ExecutorService executor = Executors.newFixedThreadPool(accessPointMetaDataList.size());

        try {
            List<DownloadTask> downloadTasks = createDownloadTasks(accessPointMetaDataList, urlRewriter, httpClient);

            List<Future<DownloadResult>> futures = submitTasksforExceution(executor, downloadTasks);

            List<DownloadResult> downloadResults = collectResultsOfTaskExecution(futures);


            for (DownloadResult downloadResult : downloadResults) {
                System.out.printf("%-40s %-80s %s \n",
                        downloadResult.getAccessPointMetaData().getCompanyName(),
                        downloadResult.getAccessPointMetaData().getUrl(),
                        downloadResult.getTaskFailureCause() == null ? "OK" : downloadResult.getTaskFailureCause().getMessage());
            }
        } finally {
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


    private List<DownloadTask> createDownloadTasks(List<AccessPointMetaData> accessPointMetaDataList, URLRewriter urlRewriter, HttpClient httpClient) {

        List<DownloadTask> downloadTasks = new ArrayList<DownloadTask>();
        for (AccessPointMetaData accessPointMetaData : accessPointMetaDataList) {

            downloadTasks.add(new DownloadTask(downloadRepository,accessPointMetaData, httpClient, urlRewriter));
        }
        return downloadTasks;
    }


    HttpClient createHttpClient() {
        // Creates a scheme registry in which all SSL peer certificates are accepted
        SchemeRegistry noSSLSecuritySchemeRegistry = createNoSSLSecuritySchemeRegistry();
        PoolingClientConnectionManager poolingClientConnectionManager = new PoolingClientConnectionManager(noSSLSecuritySchemeRegistry);

        poolingClientConnectionManager.setMaxTotal(100);

        HttpClient httpClient = new DefaultHttpClient(poolingClientConnectionManager);
        HttpParams httpParams = httpClient.getParams();
        // Timeout after 5 seconds
        HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
        // Low level socket read, should timeout after a second.
        HttpConnectionParams.setSoTimeout(httpParams, 1000);
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

        return schemeRegistry;
    }

}
