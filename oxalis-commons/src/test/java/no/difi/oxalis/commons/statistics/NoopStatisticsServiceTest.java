package no.difi.oxalis.commons.statistics;

import org.testng.annotations.Test;

public class NoopStatisticsServiceTest {

    @Test
    public void simple() {
        new NoopStatisticsService().persist(null, null, null);
    }
}
