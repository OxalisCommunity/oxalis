package eu.peppol.start.util;

import org.testng.annotations.Test;

import static eu.peppol.start.util.Time.*;
import static org.testng.Assert.*;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 9:15:41 AM
 */
@Test
public class TimeTest extends TestBase {

    public void test01() throws Throwable {
        try {

            assertEquals(milliseconds(0), 0);

            assertEquals(seconds(0), 0);
            assertEquals(seconds(1), 1000);
            assertEquals(seconds(2), 2 * 1000);

            assertEquals(minutes(0), 0);
            assertEquals(minutes(1), 60 * 1000);
            assertEquals(minutes(2), 2 * 60 * 1000);

            assertEquals(hours(0), 0);
            assertEquals(hours(1), 60 * 60 * 1000);
            assertEquals(hours(2), 2 * 60 * 60 * 1000);

            assertEquals(days(0), 0);
            assertEquals(days(1), 24 * 60 * 60 * 1000);
            assertEquals(days(2), 2 * 24 * 60 * 60 * 1000);

        } catch (Throwable t) {
            signal(t);
        }
    }

    public void test0() throws Throwable {
        try {

            assertEquals(milliseconds(1), new Time(1, MILLISECONDS).getMilliseconds());
            assertEquals(seconds(1), new Time(1, SECONDS).getMilliseconds());
            assertEquals(minutes(1), new Time(1, MINUTES).getMilliseconds());
            assertEquals(hours(1), new Time(1, HOURS).getMilliseconds());
            assertEquals(days(1), new Time(1, DAYS).getMilliseconds());

        } catch (Throwable t) {
            signal(t);
        }
    }
}
