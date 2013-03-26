package eu.peppol.statistics;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.util.Calendar;

import static java.util.Calendar.*;
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
