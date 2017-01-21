package no.difi.oxalis.api.timestamp;

import brave.Span;
import no.difi.oxalis.api.lang.TimestampException;

/**
 * @author erlend
 * @since 4.0.0
 */
@FunctionalInterface
public interface TimestampProvider {

    Timestamp generate(byte[] content) throws TimestampException;

    default Timestamp generate(byte[] content, Span span) throws TimestampException {
        return generate(content);
    }
}
