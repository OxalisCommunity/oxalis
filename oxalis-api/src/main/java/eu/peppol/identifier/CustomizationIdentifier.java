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

/**
 * Represents a PEPPOL Customization Identifier contained within a PEPPOL Document Identifier.
 *
 * @author Steinar Overbeck Cook steinar@sendregning.no
 * @author Thore Johnsen thore@sendregning.no
 *
 * @see "PEPPOL Policy for use of identifiers v3.0 of 2014-02-03"
 */
public class CustomizationIdentifier {

    private String value;

    public CustomizationIdentifier(String customizationIdentifier) {
        if (customizationIdentifier != null) customizationIdentifier = customizationIdentifier.trim();
        this.value = customizationIdentifier;
    }

    public static CustomizationIdentifier valueOf(String s) {
        return new CustomizationIdentifier(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomizationIdentifier that = (CustomizationIdentifier) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

}
