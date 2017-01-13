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

package eu.peppol.as2.outbound;

import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import eu.peppol.security.CommonName;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.Receipt;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * @author steinar
 * @author thore
 */
public class As2TransmissionResponse implements TransmissionResponse {

    final MessageId messageId;

    final PeppolStandardBusinessHeader sbdh;

    final Header header;

    final URL url;

    final BusDoxProtocol busDoxProtocol;

    final CommonName commonName;

    private final byte[] remEvidenceBytes;

    private final byte[] nativeEvidenceBytes;

    public As2TransmissionResponse(MessageId messageId, PeppolStandardBusinessHeader sbdh, URL url, BusDoxProtocol busDoxProtocol, CommonName commonName, byte[] remEvidenceBytes, byte[] nativeEvidenceBytes) {
        this.messageId = messageId;
        this.sbdh = sbdh;
        this.header = sbdh.toVefa();
        this.url = url;
        this.busDoxProtocol = busDoxProtocol;
        this.commonName = commonName;
        this.remEvidenceBytes = remEvidenceBytes;
        this.nativeEvidenceBytes = nativeEvidenceBytes;
    }

    @Override
    public PeppolStandardBusinessHeader getStandardBusinessHeader() {
        return sbdh;
    }

    @Override
    public Header getHeader() {
        return header;
    }

    public MessageId getMessageId() {
        return messageId;
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
    public byte[] getRemEvidenceBytes() {
        return remEvidenceBytes;
    }

    @Override
    public byte[] getNativeEvidenceBytes() {
        return nativeEvidenceBytes;
    }

    @Override
    public List<Receipt> getReceipts() {
        return Collections.emptyList();
    }


}
