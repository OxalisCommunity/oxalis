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

import eu.peppol.persistence.TransmissionEvidence;
import org.etsi.uri._02640.v2_.REMEvidenceType;

import javax.xml.bind.JAXBElement;
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
    private JAXBElement<REMEvidenceType> remEvidenceInstance;

    public As2RemWithMdnTransmissionEvidenceImpl(JAXBElement<REMEvidenceType> remEvidenceTypeJAXBElement) {

        this.remEvidenceInstance = remEvidenceTypeJAXBElement;
    }


    @Override
    public Date getReceptionTimeStamp() {
        return remEvidenceInstance.getValue().getEventTime().toGregorianCalendar().getTime();
    }
}
