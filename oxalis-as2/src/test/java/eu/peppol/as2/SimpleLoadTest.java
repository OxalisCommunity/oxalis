package eu.peppol.as2;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import eu.peppol.as2.inbound.As2InboundModule;
import eu.peppol.as2.outbound.As2OutboundModule;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.commons.http.ApacheHttpModule;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.statistics.StatisticsModule;
import no.difi.oxalis.commons.timestamp.TimestampModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import no.difi.oxalis.test.jetty.AbstractJettyServerTest;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimpleLoadTest extends AbstractJettyServerTest {

    private static Logger logger = LoggerFactory.getLogger(SimpleLoadTest.class);

    @Override
    public Injector getInjector() {
        return Guice.createInjector(new As2TestModule(), new As2InboundModule(), new TracingModule(),
                new StatisticsModule(), new ModeModule(), new TimestampModule(), new As2OutboundModule(),
                new ApacheHttpModule());
    }

    @Test
    public void simple() throws Exception {
        MessageSender messageSender = injector.getInstance(Key.get(MessageSender.class, Names.named("oxalis-as2")));

        TransmissionRequest transmissionRequest = new TransmissionRequest() {
            @Override
            public Endpoint getEndpoint() {
                return Endpoint.of(TransportProfile.AS2_1_0, URI.create("http://localhost:8080/as2"), null);
            }

            @Override
            public Header getHeader() {
                return Header.newInstance();
            }

            @Override
            public InputStream getPayload() {
                return getClass().getResourceAsStream("/as2-peppol-bis-invoice-sbdh.xml");
            }
        };

        long ts = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Future<TransmissionResponse>> futures = new ArrayList<>();
        for (int i = 0; i < 500; i++)
            futures.add(executorService.submit(() -> messageSender.send(transmissionRequest)));

        for (Future<TransmissionResponse> future : futures)
            future.get();

        long result = System.currentTimeMillis() - ts;
        logger.info("Sent 500 messages in {} ms.", result);

        Assert.assertTrue(result < 60000, "Sending 500 messages took more than one minute.");
    }
}
