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

package network.oxalis.statistics.util;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author steinar
 * Date: 25.03.13
 * Time: 16:08
 */
@Slf4j
public class DateParserTest {

    private String[][] testData = {
            {"2013", "2013-01-01T00"},
            {"2013-02", "2013-02-01T00"},
            {"2013-03-25", "2013-03-25T00"},
            {"2013-03-25T14", "2013-03-25T14"},
            {"2013-06-12T23:59", "2013-06-12T23"}
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

            mutableDateTime.setTime(dateTime.getHourOfDay(), 0, 0, 0);

            log.info("{} {}   {}", dateTime, dateHourFormat.print(dateTime), mutableDateTime.toDate());
            Assert.assertEquals(dateHourFormat.print(dateTime), expected);

        }
    }
}
