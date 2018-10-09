package no.difi.oxalis.ext.testbed.v1;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.lang.EvidenceException;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.util.Type;
import no.difi.oxalis.ext.testbed.v1.jaxb.InboundType;
import no.difi.vefa.peppol.common.model.Header;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author erlend
 */
@Singleton
@Type("testbed-v1")
@Slf4j
public class TestbedPersisterHandler implements PersisterHandler {

    @Inject
    @Named("rem")
    private EvidenceFactory evidenceFactory;

    @Inject
    private TestbedSender sender;

    @Override
    public Path persist(TransmissionIdentifier transmissionIdentifier, Header header, InputStream inputStream)
            throws IOException {
        Path path = File.createTempFile("oxalis-testbed-inbound", header.getIdentifier().getIdentifier()).toPath();
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            ByteStreams.copy(inputStream, outputStream);
        }
        return path;
    }

    @Override
    public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(payloadPath)) {
            InboundType inbound = new InboundType();
            inbound.setTransportProfile(inboundMetadata.getTransportProtocol().getIdentifier());
            inbound.setPayload(ByteStreams.toByteArray(inputStream));
            inbound.setReceipt(ByteStreams.toByteArray(evidenceFactory.write(inboundMetadata)));
            sender.send(inbound);
        } catch (EvidenceException e) {
            log.error(e.getMessage(), e);
        }

        Files.delete(payloadPath);
    }
}
