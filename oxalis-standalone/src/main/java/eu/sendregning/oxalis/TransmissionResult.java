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

package eu.sendregning.oxalis;

import no.difi.oxalis.api.outbound.TransmissionResponse;

/**
 * @author steinar
 *         Date: 07.01.2017
 *         Time: 22.44
 */
public class TransmissionResult {
    private final long duration;
    private final TransmissionResponse transmissionResponse;

    public TransmissionResult(long duration, TransmissionResponse transmissionResponse) {

        this.duration = duration;
        this.transmissionResponse = transmissionResponse;
    }

    public long getDuration() {
        return duration;
    }

    public TransmissionResponse getTransmissionResponse() {
        return transmissionResponse;
    }
}
