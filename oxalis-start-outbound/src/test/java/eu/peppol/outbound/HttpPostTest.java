package eu.peppol.outbound;

import eu.peppol.as2.*;
import eu.peppol.security.KeystoreManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.*;

/**
 * Sample brute force document sender, implemented by hand coding everything.
 *
 * Requires the Oxalis server to be running.
 *
 * @author steinar
 *         Date: 27.10.13
 *         Time: 13:46
 */
@Test(groups = {"integration"})
public class HttpPostTest {

    public static final String OXALIS_AS2_URL = "http://localhost:8080/oxalis/as2";

    @Test
    public void testPost() throws Exception {


        InputStream resourceAsStream = HttpPostTest.class.getClassLoader().getResourceAsStream("peppol-bis-invoice-sbdh.xml");
        assertNotNull(resourceAsStream);

        X509Certificate ourCertificate = KeystoreManager.INSTANCE.getOurCertificate();

        SmimeMessageFactory SmimeMessageFactory = new SmimeMessageFactory(KeystoreManager.INSTANCE.getOurPrivateKey(), ourCertificate);
        MimeMessage signedMimeMessage = SmimeMessageFactory.createSignedMimeMessage(resourceAsStream, new MimeType("application/xml"));


        CloseableHttpClient httpClient = createCloseableHttpClient();

        HttpPost httpPost = new HttpPost(OXALIS_AS2_URL);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        signedMimeMessage.writeTo(byteArrayOutputStream);

        As2SystemIdentifier asFrom = new As2SystemIdentifier(ourCertificate.getSubjectX500Principal());

        httpPost.addHeader(As2Header.AS2_FROM.getHttpHeaderName(), asFrom.toString());
        httpPost.addHeader(As2Header.AS2_TO.getHttpHeaderName(), "AS2-TEST");
        httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(), As2DispositionNotificationOptions.getDefault().toString());
        httpPost.addHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
        httpPost.addHeader(As2Header.SUBJECT.getHttpHeaderName(), "AS2 TEST MESSAGE");
        httpPost.addHeader(As2Header.MESSAGE_ID.getHttpHeaderName(), UUID.randomUUID().toString());
        httpPost.addHeader(As2Header.DATE.getHttpHeaderName(), As2DateUtil.format(new Date()));


        // Inserts the S/MIME message to be posted
        httpPost.setEntity(new ByteArrayEntity(byteArrayOutputStream.toByteArray(), ContentType.APPLICATION_XML));

        CloseableHttpResponse postResponse = null;      // EXECUTE !!!!
        try {
            postResponse = httpClient.execute(httpPost);
        } catch (HttpHostConnectException e) {
            fail("The Oxalis server does not seem to be running at " + OXALIS_AS2_URL);
        }

        HttpEntity entity = postResponse.getEntity();   // Any results?
        assertEquals(postResponse.getStatusLine().getStatusCode(), 200);
        String contents = EntityUtils.toString(entity);

        assertNotNull(contents);

        try {
            MimeMessage mimeMessage = MimeMessageHelper.createMimeMessage(contents);

            MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
            String msg = mdnMimeMessageInspector.getPlainText();
            System.out.println(msg);

        } finally {
            postResponse.close();
        }
    }

    private CloseableHttpClient createCloseableHttpClient() {
        SSLContext sslcontext = SSLContexts.createSystemDefault();
        // Use custom hostname verifier to customize SSL hostname verification.
        X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext, hostnameVerifier))
                .build();
        return httpclient;
    }
}
