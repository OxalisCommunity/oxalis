/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.as2;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.outbound.MessageSender;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.as2.inbound.As2InboundModule;
import network.oxalis.as2.outbound.As2OutboundModule;
import network.oxalis.commons.guice.GuiceModuleLoader;
import network.oxalis.test.jetty.AbstractJettyServerTest;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.TransportProfile;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class SimpleLoadTest extends AbstractJettyServerTest {

    private static final int MESSAGES = 500;

    @Override
    public Injector getInjector() {
        return Guice.createInjector(
                new GuiceModuleLoader(),
                new As2OutboundModule(),
                new As2InboundModule()
        );
    }

    @Test
    public void simpleSha1() throws Exception {
        MessageSender messageSender = injector.getInstance(Key.get(MessageSender.class, Names.named("oxalis-as2")));

        TransmissionRequest transmissionRequest = new TransmissionRequest() {
            @Override
            public Endpoint getEndpoint() {
                return Endpoint.of(TransportProfile.PEPPOL_AS2_1_0, URI.create("http://localhost:8080/as2"),
                        injector.getInstance(X509Certificate.class));
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
        for (int i = 0; i < MESSAGES; i++)
            futures.add(executorService.submit(() -> messageSender.send(transmissionRequest)));

        for (Future<TransmissionResponse> future : futures)
            future.get();

        long result = System.currentTimeMillis() - ts;
        log.info("Sent {} messages in {} ms.", MESSAGES, result);

        Assert.assertTrue(result < 5 * 60 * 1000,
                String.format("Sending %s messages took more than one minute.", MESSAGES));
    }

    @Test
    public void simpleSha256() throws Exception {
        MessageSender messageSender = injector.getInstance(Key.get(MessageSender.class, Names.named("oxalis-as2")));

        TransmissionRequest transmissionRequest = new TransmissionRequest() {
            @Override
            public Endpoint getEndpoint() {
                return Endpoint.of(TransportProfile.PEPPOL_AS2_2_0, URI.create("http://localhost:8080/as2"),
                        injector.getInstance(X509Certificate.class));
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
        for (int i = 0; i < MESSAGES; i++)
            futures.add(executorService.submit(() -> messageSender.send(transmissionRequest)));

        for (Future<TransmissionResponse> future : futures)
            future.get();

        long result = System.currentTimeMillis() - ts;
        log.info("Sent {} messages in {} ms.", MESSAGES, result);

        Assert.assertTrue(result < 5 * 60 * 1000,
                String.format("Sending %s messages took more than one minute.", MESSAGES));
    }
}
