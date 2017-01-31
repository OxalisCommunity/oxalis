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

package no.difi.oxalis.commons.timestamp;

import brave.Tracer;
import com.google.inject.Inject;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = {GuiceModuleLoader.class})
public class SystemTimestampProviderTest {

    @Inject
    private TimestampProvider timestampProvider;

    @Inject
    private Tracer tracer;

    @Test
    public void simpleWithoutTracer() throws Exception {
        Timestamp timestamp = timestampProvider.generate("Hello World!".getBytes());

        Assert.assertNotNull(timestamp.getDate());
        Assert.assertFalse(timestamp.getReceipt().isPresent());
    }

    @Test
    public void simpleWithTracer() throws Exception {
        Timestamp timestamp = timestampProvider.generate("Hello World!".getBytes(), tracer.newTrace());

        Assert.assertNotNull(timestamp.getDate());
        Assert.assertFalse(timestamp.getReceipt().isPresent());
    }
}
