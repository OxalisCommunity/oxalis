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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Ensures that all date time objects are parsed and formatted according to the specifications in RFC4130.
 *
 * RFC-4130 references RFC-2045, which references RFC-1123, which references good old RFC-822.
 <pre>
    date-time   =  [ day "," ] date time        ; dd mm yy
                                                ;  hh:mm:ss zzz

 day         =  "Mon"  / "Tue" /  "Wed"  / "Thu"
            /  "Fri"  / "Sat" /  "Sun"

 date        =  1*2DIGIT month 4DIGIT           ; day month year(4 digits)
                                                ;  e.g. 20 Jun 82

 month       =  "Jan"  /  "Feb" /  "Mar"  /  "Apr"
            /  "May"  /  "Jun" /  "Jul"  /  "Aug"
            /  "Sep"  /  "Oct" /  "Nov"  /  "Dec"

 time        =  hour zone                       ; ANSI and Military

 hour        =  2DIGIT ":" 2DIGIT [":" 2DIGIT]
                                                ; 00:00:00 - 23:59:59

 zone        =  "UT"  / "GMT"                   ; Universal Time
                                                ; North American : UT
            /  "EST" / "EDT"                    ;  Eastern:  - 5/ - 4
            /  "CST" / "CDT"                    ;  Central:  - 6/ - 5
            /  "MST" / "MDT"                    ;  Mountain: - 7/ - 6
            /  "PST" / "PDT"                    ;  Pacific:  - 8/ - 7
            /  1ALPHA                           ; Military: Z = UT;
                                                ;  A:-1; (J not used)
                                                ;  M:-12; N:+1; Y:+12
            / ( ("+" / "-") 4DIGIT )            ; Local differential
                                                ;  hours+min. (HHMM)
 </pre>
 * @see "RFC-4130"
 * @see "RFC-2045"
 * @see "RFC-1123"
 * @see "RFC-822"
 *
 * @author steinar
 *         Date: 22.10.13
 *         Time: 13:30
 */
public class As2DateUtil {


    private static final String rfc822DateFormat = "EEE, dd MMM yyyy HH:mm:ss Z";
    public static final String ISO8601_TS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static Date parse(String dateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(rfc822DateFormat);
        Date parsedDate = null;
        try {
            parsedDate = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parseMultipart '" + dateString + "' into a date using format '" + rfc822DateFormat + "'");
        }
        return parsedDate;
    }

    public static String format(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(rfc822DateFormat);
        return simpleDateFormat.format(date);
    }

    public static String iso8601TimeStamp() {
        String iso8601TimeStamp = new SimpleDateFormat(ISO8601_TS_FORMAT).format(new Date());
        return iso8601TimeStamp;
    }

    public static String formatIso8601(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ISO8601_TS_FORMAT);
        return simpleDateFormat.format(date);
    }

    public static Date parseIso8601TimeStamp(String dateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ISO8601_TS_FORMAT);
        Date parsedDate = null;
        try {
            parsedDate = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parse " + dateString + " as ISO8601 datetime: " + e.getMessage(),e);
        }
        return parsedDate;
    }
}