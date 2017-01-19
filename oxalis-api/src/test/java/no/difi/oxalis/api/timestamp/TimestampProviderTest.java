package no.difi.oxalis.api.timestamp;

import no.difi.oxalis.api.lang.TimestampException;
import no.difi.vefa.peppol.common.model.Receipt;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

public class TimestampProviderTest {

    @Test
    public void simple() throws TimestampException {
        TimestampProvider timestampProvider = content -> new Timestamp(new Date(), Receipt.of(content));

        Timestamp timestamp = timestampProvider.generate("Hello World!".getBytes(), null);

        Assert.assertNotNull(timestamp.getDate());
        Assert.assertNotNull(timestamp.getReceipt().get());
        Assert.assertEquals(timestamp.getReceipt().get().getValue(), "Hello World!".getBytes());
    }
}
