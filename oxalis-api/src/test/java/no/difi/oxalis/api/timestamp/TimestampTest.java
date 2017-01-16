package no.difi.oxalis.api.timestamp;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

public class TimestampTest {

    @Test
    public void simple() {
        Timestamp timestamp = new Timestamp(new Date(), null);

        Assert.assertNotNull(timestamp.getDate());
        Assert.assertFalse(timestamp.getReceipt().isPresent());
    }
}
