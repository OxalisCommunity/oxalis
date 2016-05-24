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

package eu.peppol.security;

import javax.security.auth.x500.X500Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of the Common Name (CN) of a X.509 certificate.
 *
 * @author steinar
 *         Date: 21.11.13
 *         Time: 21:55
 */
public class CommonName {

    public static final String CN_PATTERN = "CN=([^,]*),";
    public static final Pattern commonNamePattern = Pattern.compile(CN_PATTERN);

    private final String commonNameTextValue;

    public CommonName(String commonNameTextValue) {
        this.commonNameTextValue = commonNameTextValue;
    }


    /**
     * Creates a CommonName instance by extracting the Common Name (CN) attribute from the supplied X500Principal
     *
     * @param x500Principal the principal from which we extract the common name attribute
     */
    public static CommonName valueOf(X500Principal x500Principal) {
        String distinguishedName = x500Principal.getName();
        Matcher m = commonNamePattern.matcher(distinguishedName);
        if (m.find()) {
            String commonNameTextValue = m.group(1);
            return new CommonName(commonNameTextValue);
        } else {
            throw new IllegalArgumentException("Unable to extract the CN attribute from " + x500Principal.getName());
        }
    }



    @Override
    public String toString() {
        return commonNameTextValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommonName that = (CommonName) o;

        if (!commonNameTextValue.equals(that.commonNameTextValue)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return commonNameTextValue.hashCode();
    }
}
