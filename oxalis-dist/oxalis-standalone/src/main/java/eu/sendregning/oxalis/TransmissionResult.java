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

package eu.sendregning.oxalis;

import network.oxalis.api.model.TransmissionIdentifier;

/**
 * @author steinar
 *         Date: 07.01.2017
 *         Time: 22.44
 */
public class TransmissionResult {

    private final long duration;
    
    private final TransmissionIdentifier transmissionIdentifier;


    public TransmissionResult(long duration, TransmissionIdentifier transmissionIdentifier) {
        this.duration = duration;
        this.transmissionIdentifier = transmissionIdentifier;
    }

    public long getDuration() {
        return duration;
    }

    public TransmissionIdentifier getTransmissionIdentifier() {
        return transmissionIdentifier;
    }
}
