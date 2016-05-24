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

import eu.peppol.evidence.TransmissionEvidence;
import no.difi.vefa.peppol.evidence.rem.SignedRemEvidence;

import java.io.InputStream;
import java.util.Date;

/**
 * Implementation of TransmissionEvidence based upon the use of vefa-receipt in vefa-peppol.
 *
 * @author steinar
 *         Date: 01.11.2015
 *         Time: 21.26
 */
public class As2RemWithMdnTransmissionEvidenceImpl implements TransmissionEvidence {


     /**
     * Holds the REMEvidenceType generated, which represents the internal implementation of
     * a generic transport receipt.
     */
    private final SignedRemEvidence signedRemEvidence;



    public As2RemWithMdnTransmissionEvidenceImpl(SignedRemEvidence signedRemEvidence) {
        this.signedRemEvidence = signedRemEvidence;
    }


    public SignedRemEvidence getSignedRemEvidence() {
        return signedRemEvidence;
    }

    @Override
    public Date getReceptionTimeStamp() {
        return signedRemEvidence.getEventTime();
    }


    @Override
    public InputStream getInputStream() {
        return TransmissionEvidenceTransformerAs2WithRemImpl.INSTANCE.getInputStream(this);
    }
}
