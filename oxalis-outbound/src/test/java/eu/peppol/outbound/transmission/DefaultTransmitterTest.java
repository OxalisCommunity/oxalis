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

package eu.peppol.outbound.transmission;

import brave.Tracer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.outbound.guice.TestResourceModule;
import no.difi.oxalis.commons.statistics.StatisticsModule;
import no.difi.oxalis.api.statistics.StatisticsService;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.Transmitter;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import no.difi.oxalis.outbound.dummy.DummyModule;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.mockito.Mockito;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;

@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class, DummyModule.class, TracingModule.class,
        ModeModule.class, StatisticsModule.class})
public class DefaultTransmitterTest {

    @Inject
    private MessageSenderFactory messageSenderFactory;

    @Inject
    @Named("noop")
    private StatisticsService statisticsService;

    @Inject
    private Tracer tracer;

    @Test
    public void simple() throws Exception {
        TransmissionRequest transmissionRequest = Mockito.mock(TransmissionRequest.class);
        Mockito.when(transmissionRequest.getEndpoint())
                .thenReturn(Endpoint.of(TransportProfile.of("busdox-transport-dummy"), URI.create("http://localhost/"), null));
        Mockito.when(transmissionRequest.getPayload())
                .thenReturn(new ByteArrayInputStream("".getBytes()));

        Transmitter transmitter = new DefaultTransmitter(messageSenderFactory, statisticsService, tracer);
        transmitter.transmit(transmissionRequest);

    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void throwException() throws Exception {
        MessageSenderFactory messageSenderFactory = Mockito.mock(MessageSenderFactory.class);
        Mockito.when(messageSenderFactory.getMessageSender(Mockito.any(TransportProfile.class)))
                .thenThrow(new OxalisTransmissionException("From unit test"));

        TransmissionRequest transmissionRequest = Mockito.mock(TransmissionRequest.class);
        Mockito.when(transmissionRequest.getEndpoint())
                .thenReturn(Endpoint.of(TransportProfile.AS2_1_0, URI.create("http://localhost/"), null));

        Transmitter transmitter = new DefaultTransmitter(messageSenderFactory, statisticsService, tracer);
        transmitter.transmit(transmissionRequest);
    }
}
