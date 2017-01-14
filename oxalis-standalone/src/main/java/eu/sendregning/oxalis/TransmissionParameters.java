/*
 * Copyright (c) 2010 - 2017 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.sendregning.oxalis;

import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.outbound.OxalisOutboundComponent;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

/**
 * @author steinar
 *         Date: 08.01.2017
 *         Time: 13.06
 */
class TransmissionParameters {

    Optional<ParticipantId> receiver = Optional.empty();
    Optional<ParticipantId> sender = Optional.empty();
    Boolean trace = false;
    Optional<PeppolDocumentTypeId> docType = Optional.empty();
    Optional<PeppolProcessTypeId> processTypeId = Optional.empty();
    Optional<URI> destinationUrl = Optional.empty();
    Optional<BusDoxProtocol> busDoxProtocol = Optional.empty();
    Optional<String> destinationSystemId = Optional.empty();
    File evidencePath;
    OxalisOutboundComponent oxalisOutboundComponent;
    boolean useFactory = false;

    public TransmissionParameters(OxalisOutboundComponent oxalisOutboundComponent) {
        this.oxalisOutboundComponent = oxalisOutboundComponent;
    }

    public Optional<ParticipantId> getReceiver() {
        return receiver;
    }

    public void setReceiver(Optional<ParticipantId> receiver) {
        this.receiver = receiver;
    }

    public Optional<ParticipantId> getSender() {
        return sender;
    }

    public void setSender(Optional<ParticipantId> sender) {
        this.sender = sender;
    }

    public Boolean getTrace() {
        return trace;
    }

    public void setTrace(Boolean trace) {
        this.trace = trace;
    }

    public Optional<PeppolDocumentTypeId> getDocType() {
        return docType;
    }

    public void setDocType(Optional<PeppolDocumentTypeId> docType) {
        this.docType = docType;
    }

    public Optional<PeppolProcessTypeId> getProcessTypeId() {
        return processTypeId;
    }

    public void setProcessTypeId(Optional<PeppolProcessTypeId> processTypeId) {
        this.processTypeId = processTypeId;
    }

    public Optional<URI> getDestinationUrl() {
        return destinationUrl;
    }

    public void setDestinationUrl(Optional<URI> destinationUrl) {
        this.destinationUrl = destinationUrl;
    }

    public Optional<BusDoxProtocol> getBusDoxProtocol() {
        return busDoxProtocol;
    }

    public void setBusDoxProtocol(Optional<BusDoxProtocol> busDoxProtocol) {
        this.busDoxProtocol = busDoxProtocol;
    }

    public Optional<String> getDestinationSystemId() {
        return destinationSystemId;
    }

    public void setDestinationSystemId(Optional<String> destinationSystemId) {
        this.destinationSystemId = destinationSystemId;
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
}
