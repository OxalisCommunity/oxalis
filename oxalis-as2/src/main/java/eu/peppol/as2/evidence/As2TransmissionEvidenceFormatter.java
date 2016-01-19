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

package eu.peppol.as2.evidence;

import com.google.inject.Inject;
import eu.peppol.eu.peppol.evidence.TransmissionEvidence;
import no.difi.vefa.peppol.evidence.rem.RemEvidenceService;
import no.difi.vefa.peppol.evidence.rem.RemEvidenceTransformer;

import java.io.OutputStream;

/**
 * @author steinar
 *         Date: 20.11.2015
 *         Time: 16.36
 */
public class As2TransmissionEvidenceFormatter {

    private final RemEvidenceService remEvidenceService;

    @Inject
    public As2TransmissionEvidenceFormatter(RemEvidenceService remEvidenceService) {

        this.remEvidenceService = remEvidenceService;
    }

    void format(TransmissionEvidence transmissionEvidence, OutputStream outputStream) {
        RemEvidenceTransformer remEvidenceTransformer = new RemEvidenceTransformer();


        As2RemWithMdnTransmissionEvidenceImpl as2RemWithMdnTransmissionEvidence = (As2RemWithMdnTransmissionEvidenceImpl) transmissionEvidence;
        remEvidenceTransformer.toFormattedXml(as2RemWithMdnTransmissionEvidence.getSignedRemEvidence(), outputStream);
    }
}
