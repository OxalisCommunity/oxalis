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

package network.oxalis.api.lang;

import java.net.URI;

/**
 * Thrown when there is a problem related to the actual transmission protocol.
 * <p>
 * Created by soc on 17.06.2016.
 */
public class OxalisTransmissionException extends OxalisException {

    public OxalisTransmissionException(String message) {
        super(message);
    }

    public OxalisTransmissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public OxalisTransmissionException(URI url, Throwable cause) {
        super(String.format("Transmission failed to endpoint '%s'.", url), cause);
    }

    public OxalisTransmissionException(String msg, URI url, Throwable e) {
        super(String.format("%s - Transmission failed to endpoint '%s' ", msg, url), e);
    }
}
