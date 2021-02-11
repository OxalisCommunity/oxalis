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

package network.oxalis.as2.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Ensures that all date time objects are parsed and formatted according to the specifications in RFC4130.
 * <p>
 * RFC-4130 references RFC-2045, which references RFC-1123, which references good old RFC-822.
 * <pre>
 * date-time   =  [ day "," ] date time        ; dd mm yy
 * ;  hh:mm:ss zzz
 *
 * day         =  "Mon"  / "Tue" /  "Wed"  / "Thu"
 * /  "Fri"  / "Sat" /  "Sun"
 *
 * date        =  1*2DIGIT month 4DIGIT           ; day month year(4 digits)
 * ;  e.g. 20 Jun 82
 *
 * month       =  "Jan"  /  "Feb" /  "Mar"  /  "Apr"
 * /  "May"  /  "Jun" /  "Jul"  /  "Aug"
 * /  "Sep"  /  "Oct" /  "Nov"  /  "Dec"
 *
 * time        =  hour zone                       ; ANSI and Military
 *
 * hour        =  2DIGIT ":" 2DIGIT [":" 2DIGIT]
 * ; 00:00:00 - 23:59:59
 *
 * zone        =  "UT"  / "GMT"                   ; Universal Time
 * ; North American : UT
 * /  "EST" / "EDT"                    ;  Eastern:  - 5/ - 4
 * /  "CST" / "CDT"                    ;  Central:  - 6/ - 5
 * /  "MST" / "MDT"                    ;  Mountain: - 7/ - 6
 * /  "PST" / "PDT"                    ;  Pacific:  - 8/ - 7
 * /  1ALPHA                           ; Military: Z = UT;
 * ;  A:-1; (J not used)
 * ;  M:-12; N:+1; Y:+12
 * / ( ("+" / "-") 4DIGIT )            ; Local differential
 * ;  hours+min. (HHMM)
 * </pre>
 *
 * @author steinar
 * @author erlend
 * @see "RFC-4130"
 * @see "RFC-2045"
 * @see "RFC-1123"
 * @see "RFC-822"
 */
public enum As2DateUtil {

    RFC822("EEE, dd MMM yyyy HH:mm:ss Z"),
    ISO8601("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private String format;

    As2DateUtil(String format) {
        this.format = format;
    }

    public Date parse(String s) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);

        try {
            return simpleDateFormat.parse(s);
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String format(Date d) {
        return new SimpleDateFormat(format, Locale.ENGLISH).format(d);
    }
}
