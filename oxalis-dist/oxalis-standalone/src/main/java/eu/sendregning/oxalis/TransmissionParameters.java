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

package eu.sendregning.oxalis;

import network.oxalis.api.tag.Tag;
import network.oxalis.outbound.OxalisOutboundComponent;
import network.oxalis.vefa.peppol.common.model.DocumentTypeIdentifier;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import network.oxalis.vefa.peppol.common.model.ProcessIdentifier;

import java.io.File;
import java.util.Optional;

/**
 * @author steinar
 *         Date: 08.01.2017
 *         Time: 13.06
 * @author erlend
 */
class TransmissionParameters {

    private ParticipantIdentifier receiver;

    private ParticipantIdentifier sender;

    private DocumentTypeIdentifier docType;

    private ProcessIdentifier processIdentifier;

    private Endpoint endpoint;

    private File evidencePath;

    private String tag;

    private boolean useFactory;

    private OxalisOutboundComponent oxalisOutboundComponent;

    public TransmissionParameters(OxalisOutboundComponent oxalisOutboundComponent) {
        this.oxalisOutboundComponent = oxalisOutboundComponent;
    }

    public Optional<ParticipantIdentifier> getReceiver() {
        return Optional.ofNullable(receiver);
    }

    public void setReceiver(ParticipantIdentifier receiver) {
        this.receiver = receiver;
    }

    public Optional<ParticipantIdentifier> getSender() {
        return Optional.ofNullable(sender);
    }

    public void setSender(ParticipantIdentifier sender) {
        this.sender = sender;
    }

    public Optional<DocumentTypeIdentifier> getDocType() {
        return Optional.ofNullable(docType);
    }

    public void setDocType(DocumentTypeIdentifier documentTypeIdentifier) {
        this.docType = documentTypeIdentifier;
    }

    public Optional<ProcessIdentifier> getProcessIdentifier() {
        return Optional.ofNullable(processIdentifier);
    }

    public void setProcessIdentifier(ProcessIdentifier processIdentifier) {
        this.processIdentifier = processIdentifier;
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

    public Tag getTag() {
        return this.tag == null ? Tag.NONE : Tag.of(this.tag);
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
