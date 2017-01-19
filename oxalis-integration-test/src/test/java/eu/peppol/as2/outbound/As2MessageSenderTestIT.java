/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.as2.outbound;

import brave.Tracer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.as2.inbound.As2InboundModule;
import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.outbound.lookup.LookupModule;
import eu.peppol.outbound.transmission.TransmissionTestITModule;
import eu.peppol.security.KeystoreManager;
import eu.peppol.util.GlobalConfiguration;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.timestamp.TimestampModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import no.difi.oxalis.test.security.CertificateMock;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.mockito.Mockito;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URI;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 29.10.13
 *         Time: 11:35
 */
@Test(groups = {"integration"})
@Guice(modules = {TransmissionTestITModule.class, As2InboundModule.class, ModeModule.class, TracingModule.class, TimestampModule.class, LookupModule.class})
public class As2MessageSenderTestIT {

    @Inject
    @Named("sample-xml-with-sbdh")
    InputStream inputStream;

    @Inject
    @Named("invoice-to-itsligo")
    InputStream itSligoInputStream;

    @Inject
    LookupService fakeLookupService;

    @Inject
    As2MessageSender as2MessageSender;

    @Inject
    KeystoreManager keystoreManager;

    @Inject
    GlobalConfiguration globalConfiguration;

    @Inject
    private Tracer tracer;

    /**
     * Verifies that the Google Guice injection of @Named injections works as expected
     */
    @Test
    public void testInjection() throws Exception {
        assertNotNull(inputStream);
    }

    /**
     * Requires our AS2 server to be up and running at https://localhost:8080/oxalis/as2
     *
     * @throws Exception
     */
    @Test(groups = {"integration"})
    public void sendSampleMessageAndVerify() throws Exception {
        Mockito.reset(fakeLookupService);
        Mockito.when(fakeLookupService.lookup(Mockito.any(Header.class)))
                .thenReturn(Endpoint.of(TransportProfile.AS2_1_0, URI.create(TransmissionTestITModule.OUR_LOCAL_OXALIS_URL), CertificateMock.withCN("APP_1000000006")));

        String receiver = "9908:810017902";
        String sender = "9908:810017902";

        ParticipantId recipient = new ParticipantId(receiver);
        Endpoint endpoint = fakeLookupService.lookup(Header.newInstance());

        MessageId messageId = new MessageId();

        byte[] mdn = as2MessageSender.perform(
                inputStream,
                messageId,
                endpoint,
                tracer.newTrace().name("unit-test"));

        assertNotNull(mdn, "Missing native evidence in sendResult");
    }


    @Test(enabled = false)
    public void sendReallyLargeFile() throws Exception {
        Mockito.reset(fakeLookupService);
        Mockito.when(fakeLookupService.lookup(Mockito.any(Header.class)))
                .thenReturn(Endpoint.of(TransportProfile.AS2_1_0, URI.create(TransmissionTestITModule.OUR_LOCAL_OXALIS_URL), CertificateMock.withCN("APP_1000000006")));

        String receiver = "9908:810017902";
        String sender = "9908:810017902";

        ParticipantId recipient = new ParticipantId(receiver);
        Endpoint endpoint = fakeLookupService.lookup(Header.newInstance());

        // TODO: generate a really large file and transmit it.
        as2MessageSender.perform(
                inputStream,
                new MessageId(),
                endpoint,
                tracer.newTrace().name("unit-test"));
    }
}
