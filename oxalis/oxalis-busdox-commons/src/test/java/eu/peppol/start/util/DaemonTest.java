package eu.peppol.start.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 9:10:22 AM
 */
@Test
public class DaemonTest extends TestBase {

    private static final int COUNT = 5;

    public void test01() throws Throwable {
        try {

            TestDaemon daemon = new TestDaemon();
            daemon.start();

            while (daemon.getCount() < COUNT) {
                Thread.sleep(500);
            }

            assertEquals(daemon.getCount(), COUNT);

        } catch (Throwable e) {
            signal(e);
        }
    }

    private static class TestDaemon extends Daemon {

        int count;

        public int getCount() {
            return count;
        }

        public void init() {
            setAntallIterasjoner(COUNT);
            setInitialDelay(new Time(1, Time.MILLISECONDS));
            setCycleDelay(new Time(1, Time.MILLISECONDS));
        }

        public void run() {
            count++;
        }
    }
}
