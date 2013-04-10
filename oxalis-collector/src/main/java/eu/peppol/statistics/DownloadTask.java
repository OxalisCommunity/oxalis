package eu.peppol.statistics;

import eu.peppol.statistics.repository.DownloadRepository;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Represents a single download task for a given URL as specified in the supplied {@link AccessPointMetaData}
 *
 * @author steinar
 *         Date: 07.03.13
 *         Time: 16:32
 */
public class DownloadTask implements Callable<DownloadResult> {

    public static final Logger log = LoggerFactory.getLogger(DownloadTask.class);

    private final DownloadRepository downloadRepository;
    private final AccessPointMetaData accessPointMetaData;
    private final HttpClient httpClient;
    private final URL downloadUrl;
    private final URLRewriter urlRewriter;

    public DownloadTask(DownloadRepository downloadRepository, AccessPointMetaData accessPointMetaData, HttpClient httpClient, URL downloadUrl) {
        this(downloadRepository, accessPointMetaData, httpClient, downloadUrl, null);
    }

    public DownloadTask(DownloadRepository downloadRepository, AccessPointMetaData accessPointMetaData, HttpClient httpClient, URL downloadUrl, URLRewriter urlRewriter) {
        this.downloadRepository = downloadRepository;

        this.accessPointMetaData = accessPointMetaData;
        this.httpClient = httpClient;
        this.downloadUrl = downloadUrl;
        this.urlRewriter = urlRewriter;
    }


    @Override
    public DownloadResult call() throws Exception {


        // Rewrites the URL if a URL rewriter was supplied
        URL url = urlRewriter != null ? urlRewriter.rewrite(downloadUrl) : downloadUrl;

        String uri = url.toExternalForm();
        log.debug("Downloading from " + uri);

        DownloadResult result = new DownloadResult(accessPointMetaData.getAccessPointIdentifier(), url);
        try {
            HttpGet httpGet = new HttpGet(uri);

            // Executes the Http GET operation
            long startOfRequest = System.nanoTime();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            long requestEnded = System.nanoTime();

            long elapsedMs = (requestEnded - startOfRequest) / 1000000l;
            result.setElapsedTimeInMillis(elapsedMs);
            result.setHttpResultCode(httpResponse.getStatusLine().getStatusCode());

            // Retrieves the results ...
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpResponse.getStatusLine().getStatusCode() == 200 && httpEntity != null) {
                saveDownloadedContents(url, httpEntity);
            } else {
                result.setTaskFailureCause(new IllegalStateException("No http entity available, rc=" + result.getHttpResultCode() ));
            }

        } catch (Exception e) {
            log.debug("Unable to download from " + uri + "; " + e, e);
            result.setTaskFailureCause(e);
        }
        return result;

    }

    private void saveDownloadedContents(URL url, HttpEntity httpEntity) throws IOException, MimeTypeParseException {
        // TODO: Attempt to determine the content type from the headers
        String s = httpEntity.getContentType() != null ? httpEntity.getContentType().getValue() : null;

        InputStream contentInputstream = httpEntity.getContent();
        try {
            downloadRepository.saveContents(accessPointMetaData.getAccessPointIdentifier(), contentInputstream, new MimeType("text/xml"), url.toExternalForm());
        } finally {
            contentInputstream.close();
        }
    }

}
