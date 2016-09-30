/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.peppol.lang;

import javax.net.ssl.SSLHandshakeException;
import java.net.URL;

/**
 * Thrown when there is a problem related to the actual transmission protocol.
 *
 * Created by soc on 17.06.2016.
 */
public class OxalisTransmissionException extends OxalisException {
    public OxalisTransmissionException(String message) {
        super(message);
    }

    public OxalisTransmissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public OxalisTransmissionException(URL url, Throwable cause) {
        super("Transmission failed to endpoint " + url.toExternalForm(), cause);
    }

    public OxalisTransmissionException(String msg, URL url, Throwable e) {
        super(msg + " Transmission failed to endpoint: " + url.toExternalForm(), e);
    }
}
