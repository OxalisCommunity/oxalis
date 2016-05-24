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

package eu.peppol.identifier;

import eu.peppol.security.CommonName;

import java.io.Serializable;

/**
 * Unique identifier for a PEPPOL Access Point.
 *
 * This identifier is typically represented by the Common Name (CN) attribute of the distinguished name of the certificate of the Subject.
 * However; the usage of the common name is only a recommendation, not a mandatory rule.
 *
 * User: steinar
 * Date: 10.02.13
 * Time: 21:00
 */
public class AccessPointIdentifier implements Serializable {

    private final String accessPointIdentifierValue;

    public static final AccessPointIdentifier TEST = new AccessPointIdentifier("NO-TEST-AP");

    /**
     * Creates an instance using whatever text value is supplied.
     *
     * @param accessPointIdentifierValue the textual representation of the identifier
     */
    public AccessPointIdentifier(String accessPointIdentifierValue) {
        this.accessPointIdentifierValue = accessPointIdentifierValue;
    }

    public static AccessPointIdentifier valueOf(CommonName commonName) {
        return new AccessPointIdentifier(commonName.toString());
    }


    @Override
    public String toString() {
        return accessPointIdentifierValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessPointIdentifier that = (AccessPointIdentifier) o;

        if (accessPointIdentifierValue != null ? !accessPointIdentifierValue.equals(that.accessPointIdentifierValue) : that.accessPointIdentifierValue != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return accessPointIdentifierValue != null ? accessPointIdentifierValue.hashCode() : 0;
    }

}
