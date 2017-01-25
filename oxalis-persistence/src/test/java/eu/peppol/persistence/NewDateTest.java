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

package eu.peppol.persistence;

import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 18.10.2016
 *         Time: 10.36
 */
public class NewDateTest {

    @Test
    public void testNewDateStuff() {
        DateTimeFormatter isoLocalDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        String formatted1 = isoLocalDateFormatter.format(LocalDate.now());


        Date dt = new Date();

        LocalDateTime localDateTime = LocalDateTime.ofInstant(dt.toInstant(), ZoneId.systemDefault());

        LocalDate ld = LocalDate.from(localDateTime);

        assertEquals(ld.toString(), formatted1);


        String formatted2 = isoLocalDateFormatter.format(localDateTime);

        assertEquals(formatted1, formatted2);

    }
}
