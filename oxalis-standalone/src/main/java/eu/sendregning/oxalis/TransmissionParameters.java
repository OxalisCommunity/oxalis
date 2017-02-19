/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.sendregning.oxalis;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import no.difi.vefa.peppol.common.model.Endpoint;

import java.io.File;
import java.util.Optional;

/**
 * @author steinar
 *         Date: 08.01.2017
 *         Time: 13.06
 * @author erlend
 */
class TransmissionParameters {

    private ParticipantId receiver;

    private ParticipantId sender;

    private PeppolDocumentTypeId docType;

    private PeppolProcessTypeId processTypeId;

    private Endpoint endpoint;

    private File evidencePath;

    private OxalisOutboundComponent oxalisOutboundComponent;

    private boolean useFactory = false;

    public TransmissionParameters(OxalisOutboundComponent oxalisOutboundComponent) {
        this.oxalisOutboundComponent = oxalisOutboundComponent;
    }

    public Optional<ParticipantId> getReceiver() {
        return Optional.ofNullable(receiver);
    }

    public void setReceiver(ParticipantId receiver) {
        this.receiver = receiver;
    }

    public Optional<ParticipantId> getSender() {
        return Optional.ofNullable(sender);
    }

    public void setSender(ParticipantId sender) {
        this.sender = sender;
    }

    public Optional<PeppolDocumentTypeId> getDocType() {
        return Optional.ofNullable(docType);
    }

    public void setDocType(PeppolDocumentTypeId docType) {
        this.docType = docType;
    }

    public Optional<PeppolProcessTypeId> getProcessTypeId() {
        return Optional.ofNullable(processTypeId);
    }

    public void setProcessTypeId(PeppolProcessTypeId processTypeId) {
        this.processTypeId = processTypeId;
    }

    public File getEvidencePath() {
        return evidencePath;
    }

    public void setEvidencePath(File evidencePath) {
        this.evidencePath = evidencePath;
    }

    public OxalisOutboundComponent getOxalisOutboundComponent() {
        return oxalisOutboundComponent;
    }

    public boolean isUseFactory() {
        return useFactory;
    }

    public void setUseFactory(boolean useFactory) {
        this.useFactory = useFactory;
    }

    public Optional<Endpoint> getEndpoint() {
        return Optional.ofNullable(endpoint);
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
}
