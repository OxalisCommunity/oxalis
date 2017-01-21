package eu.peppol.as2;

import com.google.inject.*;
import com.google.inject.name.Names;
import eu.peppol.as2.inbound.As2InboundModule;
import eu.peppol.as2.outbound.As2OutboundModule;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.identifier.MessageId;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.statistics.RawStatisticsRepository;
import no.difi.oxalis.api.inbound.ContentPersister;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.commons.http.ApacheHttpModule;
import no.difi.oxalis.commons.inbound.NullContentPersister;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.timestamp.TimestampModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import no.difi.oxalis.test.jetty.AbstractJettyServerTest;
import no.difi.oxalis.test.security.CertificateMock;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URI;
import java.security.cert.X509Certificate;

public class SimpleServerTest extends AbstractJettyServerTest {

    @Override
    public Injector getInjector() {
        return Guice.createInjector(new As2TestModule(), new As2InboundModule(), new TracingModule(), new ModeModule(),
                new TimestampModule(), new As2OutboundModule(), new ApacheHttpModule(), new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(X509Certificate.class).toInstance(CertificateMock.withCN("APP_TEST"));
                        bind(AccessPointIdentifier.class).toInstance(new AccessPointIdentifier("APP_TEST"));
                        bind(RawStatisticsRepository.class).toInstance(Mockito.mock(RawStatisticsRepository.class));
                        bind(MessageRepository.class).toInstance(Mockito.mock(MessageRepository.class));

                        bind(ContentPersister.class).to(NullContentPersister.class).in(Singleton.class);
                    }
                });
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
