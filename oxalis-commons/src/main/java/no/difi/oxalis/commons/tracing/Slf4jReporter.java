package no.difi.oxalis.commons.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.Span;
import zipkin.reporter.Reporter;

/**
 * Implementation of ZipKin Reporter putting tracing data in SLF4J logger.
 *
 * @author erlend
 * @since 4.0.0
 */
public class Slf4jReporter implements Reporter<Span> {

    /**
     * Logger used for tracing data.
     */
    private final Logger logger = LoggerFactory.getLogger(Slf4jReporter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void report(Span span) {
        logger.info("{}", span);
    }
}
