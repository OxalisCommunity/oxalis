package no.difi.oxalis.commons.timestamp;

import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampService;

import java.util.Date;

class SystemTimestampService implements TimestampService {

    @Override
    public Timestamp generate(byte[] content) {
        return new Timestamp(new Date(), null);
    }
}
