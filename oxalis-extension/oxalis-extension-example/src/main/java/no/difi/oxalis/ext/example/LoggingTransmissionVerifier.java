package no.difi.oxalis.ext.example;

import no.difi.oxalis.api.model.Direction;
import no.difi.oxalis.api.transmission.TransmissionVerifier;
import no.difi.oxalis.api.util.Type;
import no.difi.vefa.peppol.common.model.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * This is a simple implementation of {@link TransmissionVerifier} where each message is logged.
 *
 * @author erlend
 * @since 4.0.1
 */
@Singleton
@Type("logging") // Name given to the implementation for use in configuration.
public class LoggingTransmissionVerifier implements TransmissionVerifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingTransmissionVerifier.class);

    @Override
    public void verify(Header header, Direction direction) {
        LOGGER.info("Direction: {} | Sender/Receiver: {}/{} | Instance identifier: {}",
                direction,
                header.getSender().getIdentifier(),
                header.getReceiver().getIdentifier(),
                header.getIdentifier());
    }
}
