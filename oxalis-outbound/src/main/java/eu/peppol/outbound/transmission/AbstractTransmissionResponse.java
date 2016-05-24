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

package eu.peppol.outbound.transmission;

import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.security.CommonName;

import java.net.URL;

/**
 * @author steinar
 * @author thore
 */
public class AbstractTransmissionResponse implements TransmissionResponse {

    final TransmissionId transmissionId;
    final PeppolStandardBusinessHeader sbdh;
    final URL url;
    final BusDoxProtocol busDoxProtocol;
    final CommonName commonName;
    private final byte[] evidenceBytes;

    public AbstractTransmissionResponse(TransmissionId transmissionId, PeppolStandardBusinessHeader sbdh, URL url, BusDoxProtocol busDoxProtocol, CommonName commonName, byte[] evidenceBytes) {
        this.transmissionId = transmissionId;
        this.sbdh = sbdh;
        this.url = url;
        this.busDoxProtocol = busDoxProtocol;
        this.commonName = commonName;
        this.evidenceBytes = evidenceBytes;
    }

    @Override
    public PeppolStandardBusinessHeader getStandardBusinessHeader() {
        return sbdh;
    }

    @Override
    public TransmissionId getTransmissionId() {
        return transmissionId;
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public BusDoxProtocol getProtocol() {
        return busDoxProtocol;
    }

    @Override
    public CommonName getCommonName() {
        return commonName;
    }

    @Override
    public byte[] getEvidenceBytes() {
        return evidenceBytes;
    }
}
