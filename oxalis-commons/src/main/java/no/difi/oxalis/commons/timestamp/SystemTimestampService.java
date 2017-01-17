package no.difi.oxalis.commons.timestamp;

import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampService;

import java.util.Date;

/**
 * Implementation of {@link TimestampService} simply providing timestamps using internal computer clock.
 *
 * @author erlend
 * @since 4.0.0
 */
class SystemTimestampService implements TimestampService {

    @Override
    public Timestamp generate(byte[] content) {
        return new Timestamp(new Date(), null);
    }
}
