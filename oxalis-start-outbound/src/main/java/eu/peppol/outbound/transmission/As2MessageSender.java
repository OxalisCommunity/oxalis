package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.as2.*;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.security.KeystoreManager;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.identifier.PeppolDocumentTypeId;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.UUID;

/**
 * Thread safe implementation of a {@link MessageSender}, which sends messages using the
 * AS2 protocol.
 *
 * @author steinar
 *         Date: 29.10.13
 *         Time: 11:35
 */
class As2MessageSender implements MessageSender {

    public static final Logger log = LoggerFactory.getLogger(As2MessageSender.class);

    private final SmpLookupManager smpLookupManager;

    @Inject
    public As2MessageSender(final SmpLookupManager smpLookupManager) {
        this.smpLookupManager = smpLookupManager;
    }

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(transmissionRequest.getPayload());
        TransmissionId transmissionId = send(inputStream,
                transmissionRequest.getPeppolStandardBusinessHeader().getRecipientId(),
                transmissionRequest.getPeppolStandardBusinessHeader().getSenderId(),
                transmissionRequest.getPeppolStandardBusinessHeader().getDocumentTypeIdentifier(),
                transmissionRequest.getEndpointAddress().getUrl());

        return new As2TransmissionResponse(transmissionId, transmissionRequest.getPeppolStandardBusinessHeader());
    }


    TransmissionId send(InputStream inputStream, ParticipantId recipient, ParticipantId sender, PeppolDocumentTypeId peppolDocumentTypeId, URL endpointAddress) {


        X509Certificate ourCertificate = KeystoreManager.INSTANCE.getOurCertificate();

        SMimeMessageFactory SMimeMessageFactory = new SMimeMessageFactory(KeystoreManager.INSTANCE.getOurPrivateKey(), ourCertificate);
        MimeMessage signedMimeMessage = null;
        try {
            signedMimeMessage = SMimeMessageFactory.createSignedMimeMessage(inputStream, new MimeType("application/xml"));
        } catch (MimeTypeParseException e) {
            throw new IllegalStateException("Problems with MIME types: " + e.getMessage(), e);
        }


        CloseableHttpClient httpClient = createCloseableHttpClient();

        HttpPost httpPost = new HttpPost(endpointAddress.toExternalForm());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            signedMimeMessage.writeTo(byteArrayOutputStream);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to stream S/MIME message into byte array output steram");
        }

        As2SystemIdentifier asFrom = null;
        try {
            asFrom = new As2SystemIdentifier(ourCertificate.getSubjectX500Principal());
        } catch (InvalidAs2SystemIdentifierException e) {
            throw new IllegalStateException("AS2 System Identifier could not be obtained from " + ourCertificate.getSubjectX500Principal(), e);
        }

        httpPost.addHeader(As2Header.AS2_FROM.getHttpHeaderName(), asFrom.toString());
//  Debug with OpenAS2
//        httpPost.addHeader(As2Header.AS2_FROM.getHttpHeaderName(), "OpenAS2B");

        // TODO: MUST add the correct AS2-TO header value from receivers X509 certificate
        log.warn("NOTE to self: must implement lookup of CN from receiving AP's access point");
        httpPost.addHeader(As2Header.AS2_TO.getHttpHeaderName(), "OpenAS2A");

        httpPost.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(), As2DispositionNotificationOptions.getDefault().toString());
        httpPost.addHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
        httpPost.addHeader(As2Header.SUBJECT.getHttpHeaderName(), "AS2 TEST MESSAGE");

        TransmissionId transmissionId = new TransmissionId();
        httpPost.addHeader(As2Header.MESSAGE_ID.getHttpHeaderName(), transmissionId.toString());
        httpPost.addHeader(As2Header.DATE.getHttpHeaderName(), As2DateUtil.format(new Date()));


        // Inserts the S/MIME message to be posted
        httpPost.setEntity(new ByteArrayEntity(byteArrayOutputStream.toByteArray(), ContentType.APPLICATION_XML));

        CloseableHttpResponse postResponse = null;      // EXECUTE !!!!
        try {
            log.info("Sending message to " + endpointAddress.toExternalForm());
            postResponse = httpClient.execute(httpPost);
        } catch (HttpHostConnectException e) {
            throw new IllegalStateException("The Oxalis server does not seem to be running at " + endpointAddress);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error during execution of http POST to " + endpointAddress + ": " + e.getMessage(), e);
        }

        HttpEntity entity = postResponse.getEntity();   // Any results?
        try {
            String contents = EntityUtils.toString(entity);

            if (log.isDebugEnabled()) {
                log.debug("Received: \n");
                Header[] allHeaders = postResponse.getAllHeaders();
                for (Header header : allHeaders) {
                    log.debug("" + header.getName() + ": " + header.getValue());
                }
                log.debug("\n" + contents);
                log.debug("---------------------------");
            }
            MimeMessage mimeMessage = MimeMessageHelper.parseMultipart(contents);

            MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
            String msg = mdnMimeMessageInspector.getPlainText();
            if (postResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return transmissionId;

            } else {
                log.error("AS2 transmission failed, rc=" + postResponse.getStatusLine().getStatusCode() + ", msg:" + msg);
                log.error(contents);
                throw new IllegalStateException("Transmission failed " + msg);
            }

        } catch (IOException e) {
            throw new IllegalStateException("Unable to obtain the contents of the response: " + e.getMessage(), e);
        } finally {
            try {
                postResponse.close();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to close http connection: " + e.getMessage(), e);
            }
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
