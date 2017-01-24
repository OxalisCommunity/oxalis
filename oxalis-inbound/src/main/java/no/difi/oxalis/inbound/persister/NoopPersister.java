package no.difi.oxalis.inbound.persister;

import com.google.common.io.ByteStreams;
import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.inbound.PayloadPersister;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.inbound.ReceiptPersister;
import no.difi.vefa.peppol.common.model.Header;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Implementation to be used in protocol benchmarking. This implementation does not store incoming content and metadata.
 *
 * @author erlend
 * @since 4.0.0
 */
public class NoopPersister implements PayloadPersister, ReceiptPersister {

    @Override
    public Path persist(MessageId messageId, Header header, InputStream inputStream) throws IOException {
        ByteStreams.exhaust(inputStream);
        return null;
    }

    @Override
    public Path persist(InboundMetadata inboundMetadata) throws IOException {
        return null;
    }
}
