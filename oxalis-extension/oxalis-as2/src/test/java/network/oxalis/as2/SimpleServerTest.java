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

import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import network.oxalis.api.outbound.MessageSender;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.api.persist.ReceiptPersister;
import network.oxalis.as2.util.SMimeDigestMethod;
import network.oxalis.as2.inbound.As2InboundModule;
import network.oxalis.as2.outbound.As2OutboundModule;
import network.oxalis.commons.guice.GuiceModuleLoader;
import network.oxalis.commons.guice.OxalisModule;
import network.oxalis.test.jetty.AbstractJettyServerTest;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.TransportProfile;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.cert.X509Certificate;

public class SimpleServerTest extends AbstractJettyServerTest {

    @Override
    public Injector getInjector() {
        return Guice.createInjector(
                new As2OutboundModule(),
                new As2InboundModule(),
                Modules.override(new GuiceModuleLoader()).with(new OxalisModule() {
                    @Override
                    protected void configure() {
                        bind(ReceiptPersister.class).toInstance((m, p) -> {
                    /*
                    Assert.assertEquals(
                            m.getDigest().getValue(),
                            Base64.getDecoder().decode("WJ/tC+Ijr05qtT60fByQ8LQ4l9k=")
                    );
                    */
                        });
                    }
                }));
    }

    @Test
    public void simple() throws Exception {
        MessageSender messageSender = injector.getInstance(Key.get(MessageSender.class, Names.named("oxalis-as2")));

        TransmissionResponse transmissionResponse = messageSender.send(new TransmissionRequest() {
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
        });

        Assert.assertNotNull(transmissionResponse);
    }

    @Test
    public void simpleSha256() throws Exception {
        MessageSender messageSender = injector.getInstance(Key.get(MessageSender.class, Names.named("oxalis-as2")));

        TransmissionResponse transmissionResponse = messageSender.send(new TransmissionRequest() {
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
        });

        Assert.assertNotNull(transmissionResponse);
    }

    @Test
    public void simpleSha512() throws Exception {
        MessageSender messageSender = injector.getInstance(Key.get(MessageSender.class, Names.named("oxalis-as2")));

        TransmissionResponse transmissionResponse = messageSender.send(new TransmissionRequest() {
            @Override
            public Endpoint getEndpoint() {
                return Endpoint.of(
                        SMimeDigestMethod.sha512.getTransportProfile(),
                        URI.create("http://localhost:8080/as2"),
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
        });

        Assert.assertNotNull(transmissionResponse);
    }

    @Test
    public void simpleGet() throws Exception {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://localhost:8080/as2").openConnection();

        Assert.assertTrue(new String(ByteStreams.toByteArray(urlConnection.getInputStream()))
                .contains("Hello AS2 world"));
        Assert.assertEquals(urlConnection.getResponseCode(), HttpServletResponse.SC_OK);
    }
}
