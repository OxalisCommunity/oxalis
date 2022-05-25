/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.commons.evidence;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.evidence.EvidenceFactory;
import network.oxalis.api.lang.EvidenceException;
import network.oxalis.api.model.TransmissionIdentifier;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.commons.guice.GuiceModuleLoader;
import network.oxalis.test.identifier.PeppolDocumentTypeIdAcronym;
import network.oxalis.vefa.peppol.common.code.DigestMethod;
import network.oxalis.vefa.peppol.common.model.*;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

/**
 * @author erlend
 */
@Slf4j
@Guice(modules = {GuiceModuleLoader.class})
public class RemEvidenceFactoryTest {

    @Inject
    private EvidenceFactory evidenceFactory;

    @Test
    public void simple() throws IOException, EvidenceException {
        Assert.assertNotNull(evidenceFactory);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        evidenceFactory.write(outputStream, createMockTransmissionResponse());

        log.info(new String(outputStream.toByteArray()));
    }

    @Test(expectedExceptions = EvidenceException.class)
    public void triggerException() throws IOException, EvidenceException {
        evidenceFactory.write(null, createMockTransmissionResponse());
    }

    private TransmissionResponse createMockTransmissionResponse() {
        TransmissionResponse transmissionResponse = Mockito.mock(TransmissionResponse.class);

        Date timestamp = new Date();
        Mockito.when(transmissionResponse.getTimestamp()).thenReturn(timestamp);

        Header header = Header.newInstance()
                .sender(ParticipantIdentifier.of("9908:987654321"))
                .receiver(ParticipantIdentifier.of("9908:123456789"))
                .documentType(PeppolDocumentTypeIdAcronym.INVOICE.toVefa())
                .identifier(InstanceIdentifier.generateUUID());
        Mockito.when(transmissionResponse.getHeader()).thenReturn(header);

        Digest digest = Digest.of(DigestMethod.SHA1, "Hello World".getBytes());
        Mockito.when(transmissionResponse.getDigest()).thenReturn(digest);

        TransmissionIdentifier transmissionIdentifier = TransmissionIdentifier.generateUUID();
        Mockito.when(transmissionResponse.getTransmissionIdentifier()).thenReturn(transmissionIdentifier);

        Mockito.when(transmissionResponse.getTransportProtocol()).thenReturn(TransportProtocol.INTERNAL);

        Mockito.when(transmissionResponse.getReceipts()).thenReturn(Collections.emptyList());

        return transmissionResponse;
    }
}
