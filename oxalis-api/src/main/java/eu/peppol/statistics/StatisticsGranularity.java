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

package eu.peppol.statistics;

/**
 * Represents granularity of statistics data.
 *
 * @author steinar
 *         Date: 26.03.13
 *         Time: 09:17
 */
public enum StatisticsGranularity {

    YEAR("Y"),MONTH("M"), DAY("D"), HOUR("H");

    private final String abbreviation;

    StatisticsGranularity(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public static StatisticsGranularity valueForAbbreviation(String abbreviation) {
        if (abbreviation == null) {
            throw new IllegalArgumentException("null string is an invalid abbreviation for statistics granularity");
        }

        for (StatisticsGranularity granularity : values()) {
            if (granularity.abbreviation.equalsIgnoreCase(abbreviation)) {
                return granularity;
            }
        }

        throw new IllegalArgumentException("Invalid abbreviation for statistics granularity: " + abbreviation);
    }
}
