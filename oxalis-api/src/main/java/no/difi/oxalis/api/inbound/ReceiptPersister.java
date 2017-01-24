package no.difi.oxalis.api.inbound;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author erlend
 * @since 4.0.0
 */
@FunctionalInterface
public interface ReceiptPersister {

    Path persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException;

}
