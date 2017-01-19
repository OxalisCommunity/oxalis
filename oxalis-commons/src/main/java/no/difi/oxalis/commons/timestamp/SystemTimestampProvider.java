package no.difi.oxalis.commons.timestamp;

import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;

import java.util.Date;

/**
 * Implementation of {@link TimestampProvider} simply providing timestamps using internal computer clock.
 *
 * @author erlend
 * @since 4.0.0
 */
class SystemTimestampProvider implements TimestampProvider {

    @Override
    public Timestamp generate(byte[] content) {
        return new Timestamp(new Date(), null);
    }
}
