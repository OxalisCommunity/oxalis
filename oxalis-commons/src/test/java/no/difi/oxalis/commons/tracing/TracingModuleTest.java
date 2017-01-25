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

import com.typesafe.config.Config;
import no.difi.vefa.peppol.mode.Mode;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;

public class TracingModuleTest {

    private TracingModule tracingModule = new TracingModule();

    @Test
    public void createHttpReporter() {
        Config config = Mockito.mock(Config.class);
        Mockito.doReturn("http://localhost/").when(config).getString("brave.http");

        Reporter reporter = tracingModule.getHttpReporter(config);

        Assert.assertTrue(reporter instanceof AsyncReporter);
    }
}
