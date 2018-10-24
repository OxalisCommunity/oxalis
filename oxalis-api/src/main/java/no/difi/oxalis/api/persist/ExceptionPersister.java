package no.difi.oxalis.api.persist;

import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.vefa.peppol.common.model.Header;

import java.nio.file.Path;

/**
 * @author erlend
 * @since 4.0.3
 */
@FunctionalInterface
public interface ExceptionPersister {

    void persist(TransmissionIdentifier transmissionIdentifier, Header header, Path payloadPath, Exception exception);

}
