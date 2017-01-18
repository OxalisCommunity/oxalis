package no.difi.oxalis.commons.tracing;

import no.difi.vefa.peppol.mode.Mode;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;

public class TracingModuleTest {

    private TracingModule tracingModule = new TracingModule();

    @Test
    public void simple() {
        Mode mode = Mockito.mock(Mode.class);
        Mockito.doReturn("http://localhost/").when(mode).getString("brave.http");

        Reporter reporter = tracingModule.getHttpReporter(mode);

        Assert.assertTrue(reporter instanceof AsyncReporter);
    }
}
