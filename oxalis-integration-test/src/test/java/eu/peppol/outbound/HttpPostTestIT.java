package eu.peppol.outbound;

import eu.peppol.as2.*;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.SSLContext;
import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.*;

/**
 * Sample brute force document sender, implemented by hand coding everything.
 * <p/>
 * Requires the Oxalis server to be running.
 *
 * @author steinar
 *         Date: 27.10.13
 *         Time: 13:46
 */
public class HttpPostTestIT {

    public static final String OXALIS_AS2_URL = "https://localhost:8443/oxalis/as2";
    public static final String PEPPOL_BIS_INVOICE_SBDH_XML = "peppol-bis-invoice-sbdh.xml";

    public static final Logger log = LoggerFactory.getLogger(HttpPostTestIT.class);

    @Test
    public void testPost() throws Exception {


        InputStream resourceAsStream = HttpPostTestIT.class.getClassLoader().getResourceAsStream(PEPPOL_BIS_INVOICE_SBDH_XML);
        assertNotNull(resourceAsStream, "Unable to locate resource " + PEPPOL_BIS_INVOICE_SBDH_XML + " in class path");

        X509Certificate ourCertificate = KeystoreManager.INSTANCE.getOurCertificate();

        SMimeMessageFactory SMimeMessageFactory = new SMimeMessageFactory(KeystoreManager.INSTANCE.getOurPrivateKey(), ourCertificate);
        MimeMessage signedMimeMessage = SMimeMessageFactory.createSignedMimeMessage(resourceAsStream, new MimeType("application/xml"));

        signedMimeMessage.writeTo(System.out);

        CloseableHttpClient httpClient = createCloseableHttpClient();

        HttpPost httpPost = new HttpPost(OXALIS_AS2_URL);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        signedMimeMessage.writeTo(byteArrayOutputStream);

        X500Principal subjectX500Principal = ourCertificate.getSubjectX500Principal();
        CommonName commonNameOfSender = CommonName.valueOf(subjectX500Principal);
        PeppolAs2SystemIdentifier asFrom = PeppolAs2SystemIdentifier.valueOf(commonNameOfSender);

        httpPost.addHeader(As2Header.AS2_FROM.getHttpHeaderName(), asFrom.toString());
        httpPost.addHeader(As2Header.AS2_TO.getHttpHeaderName(), new PeppolAs2SystemIdentifier(PeppolAs2SystemIdentifier.AS2_SYSTEM_ID_PREFIX+ "AS2-TEST").toString());
        httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(), As2DispositionNotificationOptions.getDefault().toString());
        httpPost.addHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
        httpPost.addHeader(As2Header.SUBJECT.getHttpHeaderName(), "AS2 TEST MESSAGE");
        httpPost.addHeader(As2Header.MESSAGE_ID.getHttpHeaderName(), UUID.randomUUID().toString());
        httpPost.addHeader(As2Header.DATE.getHttpHeaderName(), As2DateUtil.format(new Date()));

        // Inserts the S/MIME message to be posted
        httpPost.setEntity(new ByteArrayEntity(byteArrayOutputStream.toByteArray(), ContentType.create("multipart/signed")));

        CloseableHttpResponse postResponse = null;      // EXECUTE !!!!
        try {
            postResponse = httpClient.execute(httpPost);
        } catch (HttpHostConnectException e) {
            fail("The Oxalis server does not seem to be running at " + OXALIS_AS2_URL);
        }

        HttpEntity entity = postResponse.getEntity();   // Any results?
        Assert.assertEquals(postResponse.getStatusLine().getStatusCode(), 200);
        String contents = EntityUtils.toString(entity);

        assertNotNull(contents);
        if (log.isDebugEnabled()) {
            log.debug("Received: \n");
            Header[] allHeaders = postResponse.getAllHeaders();
            for (Header header : allHeaders) {
                log.debug("" + header.getName() + ": " + header.getValue());
            }
            log.debug("\n" + contents);
            log.debug("---------------------------");
        }

        try {

            MimeMessage mimeMessage = MimeMessageHelper.parseMultipart(contents);
            System.out.println("Received multipart MDN response decoded as type : " + mimeMessage.getContentType());

            // Make sure we set content type header for the multipart message (should be multipart/signed)
            String contentTypeFromHttpResponse = postResponse.getHeaders("Content-Type")[0].getValue(); // Oxalis always return only one
            mimeMessage.setHeader("Content-Type", contentTypeFromHttpResponse);
            Enumeration<String> headerlines = mimeMessage.getAllHeaderLines();
            while (headerlines.hasMoreElements()) {
                // Content-Type: multipart/signed;
                // protocol="application/pkcs7-signature";
                // micalg=sha-1;
                // boundary="----=_Part_3_520186210.1399207766925"
                System.out.println("HeaderLine : " + headerlines.nextElement());
            }

            MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
            String msg = mdnMimeMessageInspector.getPlainTextPartAsText();
            System.out.println(msg);

        } finally {
            postResponse.close();
        }
    }

    private CloseableHttpClient createCloseableHttpClient() {
        // not using PoolingHttpClientConnectionManager - just create a new httpclient
        try {
            SSLContext sslcontext = SSLContexts.custom().useTLS().build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            return httpclient;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create TLS based SSLContext", ex);
        }
    }

}
