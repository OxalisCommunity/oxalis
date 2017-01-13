/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.evidence.TransmissionEvidence;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.OxalisMessagePersistenceException;
import eu.peppol.security.KeystoreManager;
import eu.peppol.statistics.RawStatisticsRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

/**
 * Executes transmission requests by sending the payload to the requested destination.
 * Updates statistics for the transmission using the configured RawStatisticsRepository.
 * <p>
 * Will log an error if the recording of statistics fails for some reason.
 *
 * @author steinar
 * @author thore
 */
public class EvidencePersistingTransmitter extends AbstractTransmitter {

    private final MessageRepository messageRepository;

    @Inject
    public EvidencePersistingTransmitter(MessageSenderFactory messageSenderFactory, RawStatisticsRepository rawStatisticsRepository, KeystoreManager keystoreManager, MessageRepository messageRepository) {
        super(messageSenderFactory, rawStatisticsRepository, keystoreManager);
        this.messageRepository = messageRepository;
    }


    @Override
    protected void persistTransmissionResponse(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse) {

        // Persists the evidence
        TransmissionEvidence transmissionEvidence = new TransmissionEvidence() {
            @Override
            public Date getReceptionTimeStamp() {
                return new Date();
            }   // Goes into delivered column of database.

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(transmissionResponse.getRemEvidenceBytes());
            }

            @Override
            public InputStream getNativeEvidenceStream() {
                return new ByteArrayInputStream(transmissionResponse.getNativeEvidenceBytes());
            }
        };

        try {
            messageRepository.saveOutboundTransportReceipt(transmissionEvidence, transmissionResponse.getMessageId());

        } catch (OxalisMessagePersistenceException e) {
            throw new IllegalStateException("Unable to save transport evidence for " + transmissionResponse.getMessageId(), e);
        } finally {
            // Finally, save the raw statistics
            super.persistTransmissionResponse(transmissionRequest, transmissionResponse);
        }

    }
}
