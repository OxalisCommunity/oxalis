package network.oxalis.test.asd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.model.TransmissionIdentifier;
import network.oxalis.api.outbound.MessageSender;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.outbound.TransmissionResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * @author erlend
 */
@Singleton
public class AsdMessageSender implements MessageSender {

    @Inject
    private CloseableHttpClient httpClient;

    @Inject
    private Config config;

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest) throws OxalisTransmissionException {
        TransmissionIdentifier transmissionIdentifier = TransmissionIdentifier.generateUUID();

        // For use in testing when no receiver is configured.
        if (config.hasPath("oxalis.asd.sender.skip") && config.getBoolean("oxalis.asd.sender.skip")) {
            return new AsdTransmissionResponse(transmissionRequest, transmissionIdentifier);
        }

        HttpPost httpPost = new HttpPost(transmissionRequest.getEndpoint().getAddress());
        httpPost.setHeader(AsdHeaders.TRANSMISSION_ID, transmissionIdentifier.getIdentifier());
        httpPost.setEntity(new InputStreamEntity(transmissionRequest.getPayload()));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String status = response.getFirstHeader(AsdHeaders.STATUS).getValue();

            if (!"OK".equals(status))
                throw new OxalisTransmissionException(status);

            return new AsdTransmissionResponse(transmissionRequest, transmissionIdentifier);
        } catch (OxalisTransmissionException e) {
            throw e;
        } catch (Exception e) {
            throw new OxalisTransmissionException("Unable to send message.", e);
        }
    }
}
