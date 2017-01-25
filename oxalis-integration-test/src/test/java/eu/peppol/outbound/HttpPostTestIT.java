/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.peppol.outbound;

import com.google.inject.Inject;
import eu.peppol.as2.model.As2DispositionNotificationOptions;
import eu.peppol.as2.util.As2Header;
import eu.peppol.as2.util.As2DateUtil;
import eu.peppol.as2.util.MdnMimeMessageInspector;
import eu.peppol.as2.util.MimeMessageHelper;
import eu.peppol.as2.util.SMimeMessageFactory;
import eu.peppol.security.KeystoreManager;
import eu.peppol.util.OxalisKeystoreModule;
import eu.peppol.util.OxalisProductionConfigurationModule;
import no.difi.oxalis.commons.security.CertificateUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Sample brute force document sender, implemented by hand coding everything.
 * <p>
 * Requires the Oxalis server to be running.
 *
 * @author steinar
 *         Date: 27.10.13
 *         Time: 13:46
 */
@Guice(modules = {OxalisProductionConfigurationModule.class, OxalisKeystoreModule.class})
public class HttpPostTestIT {

    public static final String OXALIS_AS2_URL = IntegrationTestConstant.OXALIS_AS2_URL;
    public static final String PEPPOL_BIS_INVOICE_SBDH_XML = "peppol-bis-invoice-sbdh.xml";

    public static final Logger log = LoggerFactory.getLogger(HttpPostTestIT.class);

    @Inject
    KeystoreManager keystoreManager;

    @Test
    public void testPost() throws Exception {


        InputStream resourceAsStream = HttpPostTestIT.class.getClassLoader().getResourceAsStream(PEPPOL_BIS_INVOICE_SBDH_XML);
        assertNotNull(resourceAsStream, "Unable to locate resource " + PEPPOL_BIS_INVOICE_SBDH_XML + " in class path");

        X509Certificate ourCertificate = keystoreManager.getOurCertificate();

        SMimeMessageFactory SMimeMessageFactory = new SMimeMessageFactory(keystoreManager.getOurPrivateKey(), ourCertificate);
        MimeMessage signedMimeMessage = SMimeMessageFactory.createSignedMimeMessage(resourceAsStream, new MimeType("application/xml"));

        signedMimeMessage.writeTo(System.out);

        CloseableHttpClient httpClient = createCloseableHttpClient();

        HttpPost httpPost = new HttpPost(OXALIS_AS2_URL);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        signedMimeMessage.writeTo(byteArrayOutputStream);

        httpPost.addHeader(As2Header.AS2_FROM, CertificateUtils.extractCommonName(ourCertificate));
        httpPost.addHeader(As2Header.AS2_TO, "AS2-TEST");
        httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS, As2DispositionNotificationOptions.getDefault().toString());
        httpPost.addHeader(As2Header.AS2_VERSION, As2Header.VERSION);
        httpPost.addHeader(As2Header.SUBJECT, "AS2 TEST MESSAGE");
        httpPost.addHeader(As2Header.MESSAGE_ID, UUID.randomUUID().toString());
        httpPost.addHeader(As2Header.DATE, As2DateUtil.RFC822.format(new Date()));

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
            CloseableHttpClient httpclient = HttpClients.custom().build();
            return httpclient;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create TLS based SSLContext", ex);
        }
    }

}
