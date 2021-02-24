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

package network.oxalis.api.transmission;

import network.oxalis.api.model.TransmissionIdentifier;
import network.oxalis.vefa.peppol.common.model.*;

import java.util.Date;
import java.util.List;

/**
 * @author erlend
 * @since 4.0.0
 */
public interface TransmissionResult {

    /**
     * Transmission id assigned during transmission
     */
    TransmissionIdentifier getTransmissionIdentifier();

    Header getHeader();

    Date getTimestamp();

    Digest getDigest();

    TransportProtocol getTransportProtocol();

    /**
     * The protocol used for the transmission
     */
    TransportProfile getProtocol();

    List<Receipt> getReceipts();

    Receipt primaryReceipt();

}
