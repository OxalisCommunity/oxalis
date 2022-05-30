package network.oxalis.ext.testbed.v1;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.settings.Settings;
import network.oxalis.ext.testbed.v1.jaxb.ErrorType;
import network.oxalis.ext.testbed.v1.jaxb.InboundType;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author erlend
 */
@Singleton
@Slf4j
public class TestbedSender {

    @Inject
    private TestbedSecurity security;

    @Inject
    private Settings<TestbedConf> settings;

    @Inject
    private CloseableHttpClient httpClient;

    public void send(InboundType inbound) throws IOException {
        try {
            HttpPost httpPost = new HttpPost(settings.getString(TestbedConf.CONTROLLER));
            httpPost.setEntity(new ByteArrayEntity(prepareContent(TestbedJaxb.OBJECT_FACTORY.createInbound(inbound)), ContentType.APPLICATION_XML));

            try (CloseableHttpResponse response = send(httpPost)) {
                // No action at the moment.
            }
        } catch (JAXBException e) {
            throw new IOException("Unable to create document to send to testbed controller.", e);
        }
    }

    public void send(ErrorType error) {
        try {
            HttpPut httpPut = new HttpPut(settings.getString(TestbedConf.CONTROLLER));
            httpPut.setEntity(new ByteArrayEntity(prepareContent(TestbedJaxb.OBJECT_FACTORY.createError(error)), ContentType.APPLICATION_XML));

            try (CloseableHttpResponse response = send(httpPut)) {
                // No action at the moment.
            }
        } catch (JAXBException | IOException e) {
            log.warn("Unable to send error to testbed controller.", e);
        }
    }

    private byte[] prepareContent(JAXBElement<?> element) throws JAXBException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TestbedJaxb.marshaller().marshal(element, baos);
        return baos.toByteArray();
    }

    private CloseableHttpResponse send(HttpUriRequest request) throws IOException {
        request.addHeader("Authorization", String.format("Digest %s", security.getExpectedAuthorization()));
        return httpClient.execute(request);
    }
}
