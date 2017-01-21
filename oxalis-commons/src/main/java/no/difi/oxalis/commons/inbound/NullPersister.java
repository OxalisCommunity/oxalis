package no.difi.oxalis.commons.inbound;

import com.google.common.io.ByteStreams;
import no.difi.oxalis.api.inbound.ContentPersister;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.inbound.ReceiptPersister;
import no.difi.vefa.peppol.common.model.Header;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class NullPersister implements ContentPersister, ReceiptPersister {

    @Override
    public Path persist(Header header, InputStream inputStream) throws IOException {
        ByteStreams.exhaust(inputStream);
        return null;
    }

    @Override
    public Path persist(InboundMetadata inboundMetadata) throws IOException {
        return null;
    }
}
