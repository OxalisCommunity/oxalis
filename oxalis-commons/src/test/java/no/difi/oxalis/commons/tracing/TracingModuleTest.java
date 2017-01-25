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
