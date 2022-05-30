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

package network.oxalis.commons.timestamp;

import com.google.inject.Inject;
import io.opentracing.Tracer;
import network.oxalis.api.model.Direction;
import network.oxalis.api.timestamp.Timestamp;
import network.oxalis.api.timestamp.TimestampProvider;
import network.oxalis.commons.guice.GuiceModuleLoader;
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
        Timestamp timestamp = timestampProvider.generate("Hello World!".getBytes(), Direction.IN);

        Assert.assertNotNull(timestamp.getDate());
        Assert.assertFalse(timestamp.getReceipt().isPresent());
    }

    @Test
    public void simpleWithTracer() throws Exception {
        Timestamp timestamp = timestampProvider.generate("Hello World!".getBytes(), Direction.IN,
                tracer.buildSpan("test").start());

        Assert.assertNotNull(timestamp.getDate());
        Assert.assertFalse(timestamp.getReceipt().isPresent());
    }
}
