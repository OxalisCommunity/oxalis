package no.difi.oxalis.commons.timestamp;

import com.google.inject.Inject;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampService;
import no.difi.oxalis.commons.guice.TestOxalisKeystoreModule;
import no.difi.oxalis.commons.mode.ModeModule;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = {ModeModule.class, TimestampModule.class, TestOxalisKeystoreModule.class})
public class SystemTimestampServiceTest {

    @Inject
    private TimestampService timestampService;

    @Test
    public void simple() throws Exception {
        Timestamp timestamp = timestampService.generate("Hello World!".getBytes());

        Assert.assertNotNull(timestamp.getDate());
        Assert.assertFalse(timestamp.getReceipt().isPresent());
    }
}
