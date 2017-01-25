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

package eu.peppol.as2;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import eu.peppol.as2.inbound.As2InboundModule;
import eu.peppol.as2.outbound.As2OutboundModule;
import no.difi.oxalis.api.persist.ReceiptPersister;
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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URI;

public class SimpleServerTest extends AbstractJettyServerTest {

    @Override
    public Injector getInjector() {
        return Guice.createInjector(new As2InboundModule(), new TracingModule(), new StatisticsModule(),
                new ModeModule(), new TimestampModule(), new As2OutboundModule(), new ApacheHttpModule(),
                Modules.override(new As2TestModule()).with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ReceiptPersister.class).toInstance((m, p) -> {
                            /*
                            Assert.assertEquals(
                                    m.getDigest().getValue(),
                                    Base64.getDecoder().decode("WJ/tC+Ijr05qtT60fByQ8LQ4l9k=")
                            );
                            */
                            return null;
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
        });

        Assert.assertNotNull(transmissionResponse);
    }
}
