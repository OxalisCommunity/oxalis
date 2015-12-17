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

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 25.03.13
 *         Time: 16:08
 */
public class DateParserTest {

    String[][] testData = {
            { "2013", "2013-01-01T00"},
            { "2013-02", "2013-02-01T00"},
            { "2013-03-25", "2013-03-25T00"},
            { "2013-03-25T14", "2013-03-25T14"},
            { "2013-06-12T23:59", "2013-06-12T23"}
    };

    @Test
    public void testDateWithZeroParts() throws Exception {

        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateOptionalTimeParser();
        DateTimeFormatter dateHourFormat = ISODateTimeFormat.dateHour();

        for (String[] entry : testData) {

            String input = entry[0];
            String expected = entry[1];

            DateTime dateTime = dateTimeFormatter.parseDateTime(input);
            MutableDateTime mutableDateTime = dateTime.toMutableDateTimeISO();

            mutableDateTime.setTime(dateTime.getHourOfDay(),0,0,0);

            System.out.println(dateTime.toString() + " " + dateHourFormat.print(dateTime) + "   " + mutableDateTime.toDate());
            assertEquals(dateHourFormat.print(dateTime), expected);

        }
    }
}
