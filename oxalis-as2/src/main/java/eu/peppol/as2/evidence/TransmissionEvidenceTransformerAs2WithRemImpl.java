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

package eu.peppol.as2.evidence;

import eu.peppol.evidence.TransmissionEvidence;
import eu.peppol.evidence.TransmissionEvidenceTransformer;
import no.difi.vefa.peppol.evidence.rem.RemEvidenceTransformer;
import no.difi.vefa.peppol.evidence.rem.SignedRemEvidence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Transforms As2RemWithMdnTransmissionEvidenceImpl to and from various representations by delegating the grunt work
 * to and internal instance of RemEvidenceTransformer.
 *
 * @author steinar
 *         Date: 20.01.2016
 *         Time: 14.37
 */

enum TransmissionEvidenceTransformerAs2WithRemImpl implements TransmissionEvidenceTransformer {

    INSTANCE;

    RemEvidenceTransformer remEvidenceTransformer;

    private TransmissionEvidenceTransformerAs2WithRemImpl() {
        remEvidenceTransformer = new RemEvidenceTransformer();
    }

    @Override
    public InputStream getInputStream(TransmissionEvidence transmissionEvidence) {

        if (transmissionEvidence instanceof As2RemWithMdnTransmissionEvidenceImpl == false) {
            throw new IllegalStateException("No suppoert for transforming " + transmissionEvidence.getClass().getName());
        }

        As2RemWithMdnTransmissionEvidenceImpl as2Rem = (As2RemWithMdnTransmissionEvidenceImpl) transmissionEvidence;
        SignedRemEvidence signedRemEvidence = as2Rem.getSignedRemEvidence();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        remEvidenceTransformer.toUnformattedXml(signedRemEvidence, baos);

        return new ByteArrayInputStream(baos.toByteArray());
    }
}
