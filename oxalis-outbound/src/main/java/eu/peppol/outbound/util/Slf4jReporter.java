package eu.peppol.outbound.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.Span;
import zipkin.reporter.Reporter;

public class Slf4jReporter implements Reporter<Span> {

    private Logger logger = LoggerFactory.getLogger(Slf4jReporter.class);

    @Override
    public void report(Span span) {
        logger.info("{}", span);
    }
}
