/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package no.difi.oxalis.outbound.dummy;

import com.google.common.io.ByteStreams;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;

import java.io.IOException;

public class DummyMessageSender implements MessageSender {

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest) throws OxalisTransmissionException {
        try {
            ByteStreams.exhaust(transmissionRequest.getPayload());

            return new DummyTransmissionResponse(transmissionRequest);
        } catch (IOException e) {
            throw new OxalisTransmissionException(e.getMessage(), e);
        }
    }
}
