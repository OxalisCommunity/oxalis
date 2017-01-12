package no.difi.oxalis.api.timestamp;

import brave.Span;
import no.difi.oxalis.api.lang.TimestampException;

public interface TimestampService {

    Timestamp generate() throws TimestampException;

    default Timestamp generate(Span span) throws TimestampException {
        return generate();
    }
}
