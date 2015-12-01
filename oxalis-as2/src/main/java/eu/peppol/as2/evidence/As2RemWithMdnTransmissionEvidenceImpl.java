/*
 * Copyright (c) 2015 Steinar Overbeck Cook
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.as2.evidence;

import eu.peppol.as2.As2ReceiptData;
import eu.peppol.persistence.TransmissionEvidence;
import no.difi.vefa.peppol.evidence.rem.SignedRemEvidence;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * Implementation of TransmissionEvidence based upon the use of vefa-receipt in vefa-peppol.
 *
 * @author steinar
 *         Date: 01.11.2015
 *         Time: 21.26
 */
public class As2RemWithMdnTransmissionEvidenceImpl implements TransmissionEvidence {


    private final SignedRemEvidence signedRemEvidence;
    /**
     * Holds the REMEvidenceType generated, which represents the internal implementation of
     * a generic transport receipt.
     */
    private final As2ReceiptData as2ReceiptData;
    private final MimeMessage sMimeMesssageHoldingMdn;

    public As2RemWithMdnTransmissionEvidenceImpl(SignedRemEvidence signedRemEvidence, As2ReceiptData as2ReceiptData, MimeMessage sMimeMesssageHoldingMdn) {
        this.signedRemEvidence = signedRemEvidence;
        this.as2ReceiptData = as2ReceiptData;
        this.sMimeMesssageHoldingMdn = sMimeMesssageHoldingMdn;
    }


    public SignedRemEvidence getSignedRemEvidence() {
        return signedRemEvidence;
    }

    @Override
    public Date getReceptionTimeStamp() {
        return signedRemEvidence.getEventTime();
    }

    public As2ReceiptData getAs2ReceiptData() {
        return as2ReceiptData;
    }

    public MimeMessage getsMimeMesssageHoldingMdn() {

        try {
            return new MimeMessage(sMimeMesssageHoldingMdn);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to make a defensive copy of the S/MIME message");
        }
    }
}
