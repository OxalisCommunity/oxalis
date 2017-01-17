package no.difi.oxalis.commons.tracing;

import org.testng.annotations.Test;

public class Slf4jReporterTest {

    @Test
    public void simple() {
        new Slf4jReporter().report(null);
    }
}
