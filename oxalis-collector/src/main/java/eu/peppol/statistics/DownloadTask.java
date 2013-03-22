package eu.peppol.statistics;

import eu.peppol.statistics.repository.DownloadRepository;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import java.io.File;
import java.io.FileOutputStream;
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
    private final URLRewriter urlRewriter;

    public DownloadTask(DownloadRepository downloadRepository, AccessPointMetaData accessPointMetaData, HttpClient httpClient ) {
        this(downloadRepository,accessPointMetaData, httpClient, null);
    }

    public DownloadTask(DownloadRepository downloadRepository, AccessPointMetaData accessPointMetaData, HttpClient httpClient, URLRewriter urlRewriter) {
        this.downloadRepository = downloadRepository;

        this.accessPointMetaData = accessPointMetaData;
        this.httpClient = httpClient;
        this.urlRewriter = urlRewriter;
    }



    @Override
    public DownloadResult call() throws Exception {

        DownloadResult result = new DownloadResult(accessPointMetaData);

        URL url = urlRewriter != null ? urlRewriter.rewrite(accessPointMetaData.getUrl()) : accessPointMetaData.getUrl();

        String uri = url.toExternalForm();
        log.debug("Downloading from " + uri);

        HttpGet httpGet = new HttpGet(uri);
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                String s = httpEntity.getContentType().getValue();

                InputStream contentInputstream = httpEntity.getContent();
                try {
                    downloadRepository.saveContents(accessPointMetaData.getAccessPointIdentifier(), contentInputstream, new MimeType("text/xml"), url.toExternalForm());
                } finally {
                    contentInputstream.close();
                }
            }

            result.setDownloadedContents(httpResponse.getStatusLine().getStatusCode()+"");


        } catch (Exception e) {
//            log.warn("Unable to download from " + uri + "; " + e, e);
            result.setTaskFailureCause(e);
        }

        return result;
    }

}
