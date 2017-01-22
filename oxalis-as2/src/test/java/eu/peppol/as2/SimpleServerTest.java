package eu.peppol.as2;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import eu.peppol.as2.inbound.As2InboundModule;
import eu.peppol.as2.outbound.As2OutboundModule;
import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.inbound.ReceiptPersister;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.commons.http.ApacheHttpModule;
import no.difi.oxalis.commons.mode.ModeModule;
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
import java.util.Base64;

public class SimpleServerTest extends AbstractJettyServerTest {

    @Override
    public Injector getInjector() {
        return Guice.createInjector(new As2InboundModule(), new TracingModule(),
                new ModeModule(), new TimestampModule(), new As2OutboundModule(), new ApacheHttpModule(),
                Modules.override(new As2TestModule()).with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ReceiptPersister.class).toInstance(m -> {
                            System.out.println(Base64.getEncoder().encodeToString(m.getDigest().getValue()));
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
            public MessageId getMessageId() {
                return new MessageId();
            }

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
