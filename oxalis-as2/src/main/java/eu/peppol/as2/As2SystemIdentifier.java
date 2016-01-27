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

package eu.peppol.as2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the unique identification of an AS2 system.
 *
 * @author steinar
 *         Date: 09.10.13
 *         Time: 10:22
 */
public class As2SystemIdentifier {

    // No Quotes, no back slashes and a maximum of 128 characters
    public static final String AS2_NAME = "\\A[^\"\\\\]{1,128}\\Z";

    public static Pattern as2NamePattern = Pattern.compile(AS2_NAME);

    private final String as2Name;

    public As2SystemIdentifier(String as2Name) throws InvalidAs2SystemIdentifierException {
        if (as2Name == null) {
            throw new IllegalArgumentException("as2Name is required argument");
        }
        this.as2Name = as2Name;
        Matcher matcher = as2NamePattern.matcher(as2Name);
        if (!matcher.matches()) {
            throw new InvalidAs2SystemIdentifierException(as2Name );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        As2SystemIdentifier that = (As2SystemIdentifier) o;

        if (!as2Name.equals(that.as2Name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return as2Name.hashCode();
    }


    @Override
    public String toString() {
        return as2Name;
    }

}
