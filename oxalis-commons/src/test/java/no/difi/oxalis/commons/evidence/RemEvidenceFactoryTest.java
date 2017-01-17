package no.difi.oxalis.commons.evidence;

import com.google.inject.Inject;
import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.api.lang.EvidenceException;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.commons.guice.TestOxalisKeystoreModule;
import no.difi.oxalis.commons.mode.ModeModule;
import no.difi.vefa.peppol.common.code.DigestMethod;
import no.difi.vefa.peppol.common.model.Digest;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.InstanceIdentifier;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * @author erlend
 */
@Guice(modules = {EvidenceModule.class, ModeModule.class, TestOxalisKeystoreModule.class})
public class RemEvidenceFactoryTest {

    private static Logger logger = LoggerFactory.getLogger(RemEvidenceFactory.class);

    @Inject
    private EvidenceFactory evidenceFactory;

    @Test
    public void simple() throws EvidenceException {
        Assert.assertNotNull(evidenceFactory);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        evidenceFactory.write(outputStream, createMockTransmissionResponse());

        logger.info(new String(outputStream.toByteArray()));
    }

    @Test(expectedExceptions = EvidenceException.class)
    public void triggerException() throws EvidenceException {
        evidenceFactory.write(null, createMockTransmissionResponse());
    }

    private TransmissionResponse createMockTransmissionResponse() {
        TransmissionResponse transmissionResponse = Mockito.mock(TransmissionResponse.class);

        Date timestamp = new Date();
        Mockito.when(transmissionResponse.getTimestamp()).thenReturn(timestamp);

        Header header = Header.newInstance()
                .sender(ParticipantIdentifier.of("9908:987654321"))
                .receiver(ParticipantIdentifier.of("9908:123456789"))
                .documentType(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier().toVefa())
                .identifier(InstanceIdentifier.generateUUID());
        Mockito.when(transmissionResponse.getHeader()).thenReturn(header);

        Digest digest = Digest.of(DigestMethod.SHA1, "Hello World".getBytes());
        Mockito.when(transmissionResponse.getDigest()).thenReturn(digest);

        MessageId messageId = new MessageId();
        Mockito.when(transmissionResponse.getMessageId()).thenReturn(messageId);

        return transmissionResponse;
    }
}
