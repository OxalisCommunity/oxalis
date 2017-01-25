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

import brave.Span;
import com.google.inject.Inject;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.test.lookup.MockLookupModule;
import no.difi.oxalis.commons.statistics.StatisticsModule;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.TransmissionService;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import no.difi.oxalis.outbound.dummy.DummyModule;
import no.difi.oxalis.outbound.dummy.DummyTransmissionResponse;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = {TransmissionTestModule.class, TransmissionModule.class, ModeModule.class, MockLookupModule.class,
        DummyModule.class, TracingModule.class, StatisticsModule.class})
public class DefaultTransmissionServiceTest {

    @Inject
    private LookupService lookupService;

    @Inject
    private TransmissionService transmissionService;

    @Test
    public void simple() throws Exception {
        MockLookupModule.resetService();

        TransmissionResponse transmissionResponse = transmissionService.send(getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml"));

        Assert.assertTrue(transmissionResponse instanceof DummyTransmissionResponse);
        Assert.assertEquals(transmissionResponse.getProtocol(), TransportProfile.of("busdox-transport-dummy"));

        Assert.assertNotNull(transmissionResponse.getHeader());
        Assert.assertNotNull(transmissionResponse.getProtocol());
    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void simpleTriggerException() throws Exception {
        Mockito.reset(lookupService);
        Mockito.when(lookupService.lookup(Mockito.any(Header.class), Mockito.any(Span.class)))
                .thenThrow(new OxalisTransmissionException("From unit test."));

        transmissionService.send(getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml"));
    }
}
