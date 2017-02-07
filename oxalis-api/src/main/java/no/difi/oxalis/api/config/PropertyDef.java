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

package no.difi.oxalis.api.config;

/**
 * Property definitions, which are declared separately from the actual instances of
 * the properties.
 *
 * @author steinar
 *         Date: 14.12.2015
 *         Time: 16.52
 */
public enum PropertyDef {

    /**
     * Location of Logback configuration file for inbound server
     */
    INBOUND_LOGGING_CONFIG("oxalis.inbound.log.config"),

    /**
     * Whether overriding the properties of the transmission builder is allowed.
     */
    TRANSMISSION_BUILDER_OVERRIDE("oxalis.transmissionbuilder.override");

    /**
     * External name of property as it appears in your .properties file, i.e. with the dot notation,
     * like for instance "x.y.z = value"
     */
    private String propertyName;

    /**
     * Enum constructor
     *
     * @param propertyName name of property as it appears in your .properties file
     */
    PropertyDef(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
