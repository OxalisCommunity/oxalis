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

package network.oxalis.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.opentracing.Tracer;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.lookup.LookupService;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.outbound.Transmitter;
import network.oxalis.api.statistics.StatisticsService;
import network.oxalis.commons.error.SilentErrorTracker;
import network.oxalis.commons.guice.GuiceModuleLoader;
import network.oxalis.commons.mode.OxalisCertificateValidator;
import network.oxalis.commons.transmission.DefaultTransmissionVerifier;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.TransportProfile;
import network.oxalis.vefa.peppol.security.api.CertificateValidator;
import org.mockito.Mockito;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;

@Guice(modules = GuiceModuleLoader.class)
public class DefaultTransmitterTest {

    @Inject
    private MessageSenderFactory messageSenderFactory;

    @Inject
    @Named("noop")
    private StatisticsService statisticsService;

    @Inject
    private Tracer tracer;

    @Inject
    private LookupService lookupService;

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void simple() throws Exception {
        TransmissionRequest transmissionRequest = Mockito.mock(TransmissionRequest.class);
        Mockito.when(transmissionRequest.getEndpoint()).thenReturn(Endpoint.of(
                TransportProfile.of("busdox-transport-dummy"), URI.create("http://localhost/"), null));
        Mockito.when(transmissionRequest.getPayload())
                .thenReturn(new ByteArrayInputStream("".getBytes()));

        Transmitter transmitter = new DefaultTransmitter(messageSenderFactory, statisticsService,
                new DefaultTransmissionVerifier(), lookupService, tracer,
                new OxalisCertificateValidator(CertificateValidator.EMPTY, tracer),
                new SilentErrorTracker());
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

        Transmitter transmitter = new DefaultTransmitter(messageSenderFactory, statisticsService,
                new DefaultTransmissionVerifier(), lookupService, tracer,
                new OxalisCertificateValidator(CertificateValidator.EMPTY, tracer),
                new SilentErrorTracker());
        transmitter.transmit(transmissionRequest);
    }
}
