package no.difi.oxalis.api.inbound;

import no.difi.vefa.peppol.common.model.Header;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author erlend
 * @since 4.0.0
 */
@FunctionalInterface
public interface ContentPersister {

    Path persist(Header header, InputStream inputStream) throws IOException;

}
