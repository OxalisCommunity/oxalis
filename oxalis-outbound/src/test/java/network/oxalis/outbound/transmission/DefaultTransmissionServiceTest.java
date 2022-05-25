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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.opentracing.Span;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.lookup.LookupService;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.api.outbound.TransmissionService;
import network.oxalis.commons.guice.GuiceModuleLoader;
import network.oxalis.test.asd.AsdTransmissionResponse;
import network.oxalis.test.lookup.MockLookupModule;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.TransportProfile;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.net.SocketTimeoutException;

public class DefaultTransmissionServiceTest {

    private Injector injector = Guice.createInjector(
            Modules.override(new GuiceModuleLoader()).with(new MockLookupModule()));

    private LookupService lookupService;

    private TransmissionService transmissionService;

    @BeforeClass
    public void beforeClass() {
        lookupService = injector.getInstance(LookupService.class);
        transmissionService = injector.getInstance(TransmissionService.class);
    }

    @Test
    public void simple() throws Exception {
        MockLookupModule.resetService();

        TransmissionResponse transmissionResponse = transmissionService.send(getClass().getResourceAsStream("/peppol-bis-invoice-sbdh.xml"));

        Assert.assertTrue(transmissionResponse instanceof AsdTransmissionResponse);
        Assert.assertEquals(transmissionResponse.getProtocol(), TransportProfile.of("bdx-transport-asd"));

        Assert.assertNotNull(transmissionResponse.getHeader());
        Assert.assertNotNull(transmissionResponse.getProtocol());
    }

    @Test(expectedExceptions = OxalisTransmissionException.class, enabled = false)
    public void simpleTriggerException() throws Exception {
        Mockito.reset(lookupService);
        Mockito.when(lookupService.lookup(Mockito.any(Header.class), Mockito.any(Span.class)))
                .thenThrow(new OxalisTransmissionException("From unit test."));

        transmissionService.send(getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml"));
    }

    @Test(expectedExceptions = SocketTimeoutException.class, enabled = false)
    public void socketTimeoutException() throws Exception {
        MockLookupModule.resetService();

        TransmissionResponse transmissionResponse = transmissionService.send(getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml"));

        Assert.assertTrue(transmissionResponse instanceof AsdTransmissionResponse);
        Assert.assertEquals(transmissionResponse.getProtocol(), TransportProfile.of("bdx-transport-asd"));

        Assert.assertNotNull(transmissionResponse.getHeader());
        Assert.assertNotNull(transmissionResponse.getProtocol());
    }
}
