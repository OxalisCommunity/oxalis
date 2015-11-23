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

import com.google.inject.Inject;
import eu.peppol.persistence.TransmissionEvidence;
import no.difi.vefa.peppol.evidence.rem.RemEvidenceService;
import no.difi.vefa.peppol.evidence.rem.RemEvidenceTransformer;
import org.etsi.uri._02640.v2_.REMEvidenceType;

import javax.xml.bind.JAXBElement;
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
        RemEvidenceTransformer remEvidenceTransformer = remEvidenceService.createRemEvidenceTransformer();

        As2RemWithMdnTransmissionEvidenceImpl as2RemWithMdnTransmissionEvidence = (As2RemWithMdnTransmissionEvidenceImpl) transmissionEvidence;
        JAXBElement<REMEvidenceType> remEvidenceInstance = as2RemWithMdnTransmissionEvidence.getRemEvidenceInstance();
        remEvidenceTransformer.setFormattedOutput(false);
        remEvidenceTransformer.transformToXml(remEvidenceInstance, outputStream);
    }
}
