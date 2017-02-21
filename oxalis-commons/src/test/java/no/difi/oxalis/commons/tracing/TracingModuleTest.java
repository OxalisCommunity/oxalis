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

package no.difi.oxalis.commons.tracing;

import brave.Tracer;
import com.github.kristofa.brave.Brave;
import no.difi.oxalis.api.settings.Settings;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;

public class TracingModuleTest {

    private TracingModule tracingModule = new TracingModule();

    @Test
    public void createHttpReporter() {
        Settings<TracingConf> settings = Mockito.mock(Settings.class);
        Mockito.doReturn("http://localhost/").when(settings).getString(TracingConf.HTTP);

        Reporter reporter = tracingModule.getHttpReporter(settings);

        Assert.assertTrue(reporter instanceof AsyncReporter);

        Tracer tracer = tracingModule.getTracer(reporter);

        Assert.assertNotNull(tracer);

        Brave brave = tracingModule.getBrave(tracer);

        Assert.assertNotNull(brave);
    }
}
