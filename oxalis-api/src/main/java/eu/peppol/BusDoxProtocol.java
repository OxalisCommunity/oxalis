/*
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

package eu.peppol;

/**
 * Finite set of known BusDox transport protocols.
 * Updated according to Policy for use of Identifiers v3.0 (2014-02-03)
 *
 * During test-phase of AS2 we allow for variations of the AS2 transport profile identifier.
 * Some SMP's still use "busdox-transport-as2-100" or just "busdox-transport-as2" instead of
 * the final "busdox-transport-as2-ver1p0" from the Policy for use of Identifiers v3.
 *
 * @author steinar
 * @author thore
 */
public enum BusDoxProtocol {

    AS2("busdox-transport-as2-ver1p0");

    private final String protocolName;

    BusDoxProtocol(String protocolName) {
        this.protocolName = protocolName;
    }

    public static BusDoxProtocol instanceFrom(String transportProfile) {
        if (transportProfile == null) transportProfile = "";
        if (transportProfile.startsWith("busdox-transport-as2")) transportProfile = AS2.protocolName; // allow variations
        for (BusDoxProtocol busDoxProtocol : values()) {
            if (busDoxProtocol.protocolName.equalsIgnoreCase(transportProfile) || busDoxProtocol.name().equalsIgnoreCase(transportProfile)) {
                return busDoxProtocol;
            }
        }
        throw new IllegalStateException("Unknown protocol name : " + transportProfile);
    }

}
