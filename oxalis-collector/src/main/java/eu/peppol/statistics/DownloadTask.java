package eu.peppol.statistics;

import eu.peppol.security.OxalisCipher;
import eu.peppol.security.OxalisCipherConverter;
import eu.peppol.statistics.repository.DownloadRepository;
import org.apache.http.Header;
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
import java.security.PrivateKey;
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
    private final PrivateKey privateKey;
    private final AccessPointMetaData accessPointMetaData;
    private final HttpClient httpClient;
    private final URL downloadUrl;
    private final URLRewriter urlRewriter;


    public DownloadTask(DownloadRepository downloadRepository, PrivateKey privateKey, AccessPointMetaData accessPointMetaData, HttpClient httpClient, URL downloadUrl, URLRewriter urlRewriter) {
        this.downloadRepository = downloadRepository;
        this.privateKey = privateKey;
        this.accessPointMetaData = accessPointMetaData;
        this.httpClient = httpClient;
        this.downloadUrl = downloadUrl;
        this.urlRewriter = urlRewriter;
    }

    public DownloadTask(DownloadRepository downloadRepository, PrivateKey privateKey, AccessPointMetaData accessPointMetaData, HttpClient httpClient, URL downloadUrl) {
        this(downloadRepository, privateKey, accessPointMetaData, httpClient, downloadUrl, null);
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
                saveDownloadedContents(url, httpResponse);
            } else {
                result.setTaskFailureCause(new IllegalStateException("No http entity available, rc=" + result.getHttpResultCode() ));
            }

        } catch (Exception e) {
            log.debug("Unable to download from " + uri + "; " + e, e);
            result.setTaskFailureCause(e);
        }
        return result;

    }

    private void saveDownloadedContents(URL url, HttpResponse httpResponse) throws IOException, MimeTypeParseException {
        // TODO: Attempt to determine the content type from the headers
        String s = httpResponse.getEntity().getContentType() != null ? httpResponse.getEntity().getContentType().getValue() : null;

        InputStream contentInputStream = getDecryptedInputStream(httpResponse);

        try {
            downloadRepository.saveContents(accessPointMetaData.getAccessPointIdentifier(), contentInputStream, new MimeType("text/xml"), url.toExternalForm());
        } finally {
            contentInputStream.close();
        }
    }

    private InputStream getDecryptedInputStream(HttpResponse httpResponse) throws IOException {

        // Retrieves the encrypted (wrapped) symmetric key from the HTTP response
        Header firstHeader = httpResponse.getFirstHeader(OxalisCipher.WRAPPED_SYMMETRIC_KEY_NAME);
        if (firstHeader == null) {
            throw new IllegalStateException("The HTTP header " + OxalisCipher.WRAPPED_SYMMETRIC_KEY_NAME + " is missing");
        }
        String wrappedSymmetricKeyInHexText = firstHeader.getValue();
        if (wrappedSymmetricKeyInHexText == null) {
            throw new IllegalStateException("No HTTP header named '" + OxalisCipher.WRAPPED_SYMMETRIC_KEY_NAME + "' found in HTTP response");
        }

        // Transforms the wrapped symmetric key (SecretKey) into an instance of OxalisCipher
        OxalisCipherConverter oxalisCipherConverter = new OxalisCipherConverter();
        OxalisCipher oxalisCipher = oxalisCipherConverter.createCipherFromWrappedHexKey(wrappedSymmetricKeyInHexText, privateKey);

        // Wraps the encrypted entity, represented by an input stream, into an InputStream which provides plain text
        InputStream inputStream = httpResponse.getEntity().getContent();
        return oxalisCipher.decryptStream(inputStream);
    }

}
