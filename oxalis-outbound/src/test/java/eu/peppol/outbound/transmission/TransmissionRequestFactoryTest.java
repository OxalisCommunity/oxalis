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

import eu.peppol.outbound.As2PrioritizedTransportModule;
import eu.peppol.outbound.lookup.LookupModule;
import eu.peppol.outbound.lookup.MockLookupModule;
import eu.peppol.outbound.guice.TestResourceModule;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.commons.http.ApacheHttpModule;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;

@Guice(modules = {TransmissionTestModule.class, TestResourceModule.class, TracingModule.class, ModeModule.class,
        LookupModule.class, ApacheHttpModule.class, As2PrioritizedTransportModule.class})
public class TransmissionRequestFactoryTest {

    @Inject
    private TransmissionRequestFactory transmissionRequestFactory;

    @Test
    public void simple() throws Exception {
        MockLookupModule.resetService();

        TransmissionRequest transmissionRequest;
        try (InputStream inputStream = getClass().getResourceAsStream("/simple-sbd.xml")) {
            transmissionRequest = transmissionRequestFactory.newInstance(inputStream);
        }

        Assert.assertNotNull(transmissionRequest.getHeader());
        Assert.assertNotNull(transmissionRequest.getEndpoint());
    }
}
