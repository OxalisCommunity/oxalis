package no.difi.oxalis.api.timestamp;

import no.difi.oxalis.api.lang.TimestampException;
import no.difi.vefa.peppol.common.model.Receipt;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

public class TimestampServiceTest {

    @Test
    public void simple() throws TimestampException {
        TimestampService timestampService = content -> new Timestamp(new Date(), Receipt.of(content));

        Timestamp timestamp = timestampService.generate("Hello World!".getBytes(), null);

        Assert.assertNotNull(timestamp.getDate());
        Assert.assertNotNull(timestamp.getReceipt().get());
        Assert.assertEquals(timestamp.getReceipt().get().getValue(), "Hello World!".getBytes());
    }
}
