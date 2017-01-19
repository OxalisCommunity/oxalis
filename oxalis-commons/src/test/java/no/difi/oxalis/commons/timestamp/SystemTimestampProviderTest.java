package no.difi.oxalis.commons.timestamp;

import brave.Tracer;
import com.google.inject.Inject;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.commons.guice.TestOxalisKeystoreModule;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.oxalis.commons.tracing.TracingModule;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = {ModeModule.class, TracingModule.class, TimestampModule.class, TestOxalisKeystoreModule.class})
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
