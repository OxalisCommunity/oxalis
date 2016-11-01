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
