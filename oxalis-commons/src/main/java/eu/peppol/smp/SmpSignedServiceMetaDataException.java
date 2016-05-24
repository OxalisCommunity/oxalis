/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
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

package eu.peppol.smp;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;

import java.net.URL;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class SmpSignedServiceMetaDataException extends Exception {

    private final ParticipantId participant;
    private final PeppolDocumentTypeId documentTypeIdentifier;
    private final URL smpUrl;

    public SmpSignedServiceMetaDataException(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier, URL smpUrl, Exception e) {
        super("Unable to find information for participant: " + participant + ", documentType: " + documentTypeIdentifier + ", at url: " + smpUrl + " ; " + e.getMessage(), e);
        this.participant = participant;
        this.documentTypeIdentifier = documentTypeIdentifier;
        this.smpUrl = smpUrl;
    }

    public ParticipantId getParticipant() {
        return participant;
    }

    public PeppolDocumentTypeId getDocumentTypeIdentifier() {
        return documentTypeIdentifier;
    }

    public URL getSmpUrl() {
        return smpUrl;
    }

}
