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
public interface TransmissionResponse {

    /**
     * Transmission id assigned during transmission
     */
    TransmissionId getTransmissionId();

    /**
     * Get the effective SBDH used to decide transmission
     */
    public PeppolStandardBusinessHeader getStandardBusinessHeader();

    /**
     * The destination URL for the transmission
     */
    public URL getURL();

    /**
     * The protocol used for the transmission
     */
    public BusDoxProtocol getProtocol();

    /**
     * The common name of the receiver certificate
     */
    public CommonName getCommonName();

    byte[] getEvidenceBytes();
}
