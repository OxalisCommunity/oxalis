package no.difi.oxalis.commons.tracing;

import brave.Tracer;
import org.testng.Assert;
import org.testng.annotations.Test;
import zipkin.reporter.Reporter;

public class TraceableTest {

    @Test
    public void simple() {
        Tracer tracer = Tracer.newBuilder().reporter(Reporter.NOOP).build();

        Traceable traceable = new Traceable(tracer) {
        };

        Assert.assertTrue(tracer == traceable.tracer);
    }
}
